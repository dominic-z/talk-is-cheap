package webflux.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import webflux.message.pojo.ChunkUploadResponse;
import webflux.message.pojo.FileStatusResponse;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Service
public class FileStorageService {

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final String DATA_SUFFIX = ".data";
    private static final String PART_SUFFIX = ".part";
    private static final String META_SUFFIX = ".properties";

    private final Path storageRoot;

    public FileStorageService() {
        this.storageRoot = Path.of(System.getProperty("user.dir"), "target", "demo-file-store").toAbsolutePath();
    }

    public Mono<FileStatusResponse> getStatus(String fileHash, String fileName, long size) {
        return Mono.fromCallable(() -> {
            Files.createDirectories(storageRoot);
            Path completed = completedPath(fileHash);
            if (Files.exists(completed)) {
                Properties meta = readMeta(fileHash);
                String storedName = meta.getProperty("fileName", fileName);
                long storedSize = parseLong(meta.getProperty("size"), Files.size(completed));
                return status(fileHash, storedName, storedSize, true, storedSize);
            }

            Path part = partPath(fileHash);
            long uploadedBytes = Files.exists(part) ? Files.size(part) : 0;
            return status(fileHash, fileName, size, false, uploadedBytes);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ChunkUploadResponse> saveChunk(String fileHash, String fileName, long size, long chunkStart,
                                               Flux<DataBuffer> body) {
        return Mono.fromCallable(() -> {
                    Files.createDirectories(storageRoot);
                    Path completed = completedPath(fileHash);
                    if (Files.exists(completed)) {
                        long completedSize = Files.size(completed);
                        return new ChunkUploadResponse(fileHash, completedSize, true, downloadUrl(fileHash));
                    }

                    Path part = partPath(fileHash);
                    long currentSize = Files.exists(part) ? Files.size(part) : 0;
                    if (chunkStart != currentSize) {
                        throw new IllegalArgumentException("chunkStart must equal current uploaded bytes: " + currentSize);
                    }

                    return new ChunkUploadResponse(fileHash, 0, false, downloadUrl(fileHash));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(resp -> {
                    if (resp != null && resp.completed()) {
                        return Mono.just(resp);
                    }

                    return Mono.usingWhen(
                            openPartChannel(fileHash),
                            channel -> DataBufferUtils.write(body, channel, chunkStart)
                                    // doOnNext会导致异常，而改为下面的map则不会，这是因为，DataBufferUtils.write(...) 会把同一个 DataBuffer 往下游继续发。你这里 doOnNext(...) 手动释放了它；紧接着 then(...) 会丢弃上游元素，只保留完成信号。对于 DataBuffer，丢弃/取消路径也可能触发释放
                                    // 因此重复释放
//                                    .doOnNext(DataBufferUtils.releaseConsumer())
                                    .map(dataBuffer -> {
                                        DataBufferUtils.release(dataBuffer);
                                        return 0;
                                    })

                                    .then(Mono.fromCallable(() -> finishIfComplete(fileHash, fileName, size))
                                            .subscribeOn(Schedulers.boundedElastic()))
                            ,
                            channel -> Mono.fromRunnable(closeQuietly(channel)).subscribeOn(Schedulers.boundedElastic())
                    );
                });
    }

    public Mono<Void> writeDownload(String fileHash, List<HttpRange> ranges, ServerHttpResponse response) {
        return Mono.fromCallable(() -> {
                    Path file = completedPath(fileHash);
                    if (!Files.exists(file)) {
                        throw new IllegalArgumentException("file not found: " + fileHash);
                    }
                    Properties meta = readMeta(fileHash);
                    FileInfo info = new FileInfo(
                            file,
                            meta.getProperty("fileName", fileHash),
                            parseLong(meta.getProperty("size"), Files.size(file))
                    );
                    return info;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(info -> {
                    Range range = resolveRange(ranges, info.size());
                    setDownloadHeaders(response, info, range);

                    if (response instanceof ZeroCopyHttpOutputMessage zeroCopy) {
                        return zeroCopy.writeWith(info.path().toFile(), range.start(), range.count());
                    }

                    Flux<DataBuffer> content = DataBufferUtils.readAsynchronousFileChannel(
                            () -> AsynchronousFileChannel.open(info.path(), StandardOpenOption.READ),
                            range.start(),
                            response.bufferFactory(),
                            BUFFER_SIZE
                    );
                    return response.writeWith(DataBufferUtils.takeUntilByteCount(content, range.count()));
                });
    }

    private Mono<AsynchronousFileChannel> openPartChannel(String fileHash) {
        return Mono.fromCallable(() -> AsynchronousFileChannel.open(
                partPath(fileHash),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        )).subscribeOn(Schedulers.boundedElastic());
    }

    private ChunkUploadResponse finishIfComplete(String fileHash, String fileName, long size) throws IOException {
        Path part = partPath(fileHash);
        long uploadedBytes = Files.exists(part) ? Files.size(part) : 0;
        if (uploadedBytes < size) {
            writeMeta(fileHash, fileName, size);
            return new ChunkUploadResponse(fileHash, uploadedBytes, false, null);
        }
        if (uploadedBytes > size) {
            throw new IllegalArgumentException("uploaded bytes exceed declared size");
        }

        moveCompleted(part, completedPath(fileHash));
        writeMeta(fileHash, fileName, size);
        return new ChunkUploadResponse(fileHash, uploadedBytes, true, downloadUrl(fileHash));
    }

    private void moveCompleted(Path part, Path completed) throws IOException {
        try {
            Files.move(part, completed, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(part, completed, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void setDownloadHeaders(ServerHttpResponse response, FileInfo info, Range range) {
        response.setStatusCode(range.partial() ? org.springframework.http.HttpStatus.PARTIAL_CONTENT :
                org.springframework.http.HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
        response.getHeaders().setContentLength(range.count());
        response.getHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");
        response.getHeaders().setContentDisposition(ContentDisposition.attachment()
                .filename(info.fileName(), StandardCharsets.UTF_8)
                .build());
        if (range.partial()) {
            response.getHeaders().set("Content-Range", "bytes " + range.start() + "-" + range.end() + "/" + info.size());
        }
    }

    private Range resolveRange(List<HttpRange> ranges, long size) {
        if (ranges == null || ranges.isEmpty()) {
            return new Range(0, size - 1, size, false);
        }
        HttpRange httpRange = ranges.get(0);
        long start = httpRange.getRangeStart(size);
        long end = httpRange.getRangeEnd(size);
        if (start < 0 || end < start || end >= size) {
            throw new IllegalArgumentException("invalid range");
        }
        return new Range(start, end, end - start + 1, true);
    }

    private FileStatusResponse status(String fileHash, String fileName, long size, boolean completed, long uploadedBytes) {
        return new FileStatusResponse(fileHash, fileName, size, completed, uploadedBytes,
                completed ? downloadUrl(fileHash) : null);
    }

    private String downloadUrl(String fileHash) {
        return "/api/files/" + fileHash + "/download";
    }

    private Path completedPath(String fileHash) {
        return storageRoot.resolve(safeName(fileHash) + DATA_SUFFIX);
    }

    private Path partPath(String fileHash) {
        return storageRoot.resolve(safeName(fileHash) + PART_SUFFIX);
    }

    private Path metaPath(String fileHash) {
        return storageRoot.resolve(safeName(fileHash) + META_SUFFIX);
    }

    private String safeName(String fileHash) {
        if (fileHash == null || !fileHash.matches("[a-fA-F0-9]{64}")) {
            throw new IllegalArgumentException("fileHash must be a sha-256 hex string");
        }
        return fileHash.toLowerCase();
    }

    private Properties readMeta(String fileHash) throws IOException {
        Properties properties = new Properties();
        Path meta = metaPath(fileHash);
        if (Files.exists(meta)) {
            try (var in = Files.newInputStream(meta)) {
                properties.load(in);
            }
        }
        return properties;
    }

    private void writeMeta(String fileHash, String fileName, long size) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("fileHash", fileHash);
        properties.setProperty("fileName", Objects.toString(fileName, fileHash));
        properties.setProperty("size", String.valueOf(size));
        properties.setProperty("createdAt", String.valueOf(System.currentTimeMillis()));
        try (var out = Files.newOutputStream(metaPath(fileHash), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            properties.store(out, "demo upload metadata");
        }
    }

    private long parseLong(String value, long fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private Runnable closeQuietly(AsynchronousFileChannel channel) {
        return () -> {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        };
    }

    private record FileInfo(Path path, String fileName, long size) {
    }

    private record Range(long start, long end, long count, boolean partial) {
    }
}
