package resources.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


/**
 * 用浏览器的html可以
 * 或者用postman也可以
 *
 * curl --location 'http://localhost:8082/upload' \
 * --form 'file=@"/home/dominiczhu/Downloads/yiibaidb.zip"' \
 * --form 'test="aaa"'
 */
@RestController
@Slf4j
public class FileUploadController {

    private static final String UPLOAD_DIR = "git_ignore";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,@RequestParam(value = "test",required = false) String test) {
        log.info("test : {}",test);
        if (file.isEmpty()) {
            return new ResponseEntity<>("请选择要上传的文件", HttpStatus.BAD_REQUEST);
        }
        try {
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            // 获取文件名
            String fileName = file.getOriginalFilename();
            // 构建文件保存路径
            File dest = new File(uploadDir.getAbsolutePath() + File.separator + fileName);
            // 保存文件
            file.transferTo(dest);
            return new ResponseEntity<>("文件上传成功", HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
