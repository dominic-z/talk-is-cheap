package resources.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


@RestController
@Slf4j
public class FileDownloadController {

    private static final String DOWNLOAD_DIR = "git_ignore";

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile() {
        File file = Paths.get(DOWNLOAD_DIR+"/SampleVideo_720x480_1mb.mp4").toFile();
        Resource resource = new FileSystemResource(file);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(value = "/download_file_streaming_via_response_entity", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> downloadFileStreamingViaResponseEntity() throws IOException{
        // 创建一个线程安全的管道，用于生产者(数据库读取)和消费者(HTTP响应)之间的通信
        // https://www.qianwen.com/share/chat/a40dc4be820644619d64b3ee2cd4bed3
        PipedInputStream pipedIn = new PipedInputStream();
        PipedOutputStream pipedOut = new PipedOutputStream(pipedIn);

        // 在新线程中执行耗时的数据读取和写入操作
        Thread dataWriterThread = new Thread(() -> {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(pipedOut, StandardCharsets.UTF_8))) {
                writer.println("订单ID,订单名称,下单人");
                writer.flush();

                for (int i=0;i<100;i++) {
                    String line = String.format("%d,%s,%s%n", i, "orderName:"+i, "orderPerson"+i+"name");
                    writer.print(line);
                    if (i % 10 == 0) {
                        writer.flush();
                    }
                }
                writer.flush();
            } catch (Exception e) {
                System.err.println("Error generating file content: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    pipedOut.close(); // 关闭输出端，通知输入端没有更多数据
                } catch (IOException e) {
                    System.err.println("Error closing piped output stream: " + e.getMessage());
                }
            }
        });

        dataWriterThread.start();

        // 创建Resource
        InputStreamResource resource = new InputStreamResource(pipedIn);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orders_response_entity.txt\"");

        // 返回ResponseEntity
        return ResponseEntity.ok()
                .headers(headers)
//                .contentLength(-1) // 对于未知大小的流，可以设置为 -1
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}
