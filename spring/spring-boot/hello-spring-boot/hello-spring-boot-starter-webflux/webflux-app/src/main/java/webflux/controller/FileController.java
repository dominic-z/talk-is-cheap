package webflux.controller;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.message.pojo.ChunkUploadResponse;
import webflux.message.pojo.FileStatusResponse;
import webflux.service.FileStorageService;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/status")
    public Mono<FileStatusResponse> status(@RequestParam String fileHash,
                                           @RequestParam String fileName,
                                           @RequestParam long size) {
        return fileStorageService.getStatus(fileHash, fileName, size);
    }

    @PostMapping("/chunk")
    public Mono<ChunkUploadResponse> uploadChunk(@RequestParam String fileHash,
                                                 @RequestParam String fileName,
                                                 @RequestParam long size,
                                                 @RequestHeader("X-Chunk-Start") long chunkStart,
                                                 @RequestBody Flux<DataBuffer> body) {
        return fileStorageService.saveChunk(fileHash, fileName, size, chunkStart, body);
    }

    @GetMapping("/{fileHash}/download")
    public Mono<Void> download(@PathVariable String fileHash,
                               @RequestHeader HttpHeaders headers,
                               ServerHttpResponse response) {
        List<HttpRange> ranges = headers.getRange();
        return fileStorageService.writeDownload(fileHash, ranges, response);
    }
}
