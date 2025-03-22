package com.example.springboot.hellospringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/uploadSingle")
    public String handleSingleFileUpload(@RequestParam("file") MultipartFile file,
                                         RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "请选择要上传的文件");
            return "ok";
        }

        try {
            byte[] bytes = file.getBytes();
            // 对应的路径就是talk-is-cheap/spring/spring-boot/hello-spring-boot/hello-spring-boot-application/uploads/
            Path path = Paths.get(UPLOAD_DIR + "/" + file.getOriginalFilename());
            Files.write(path, bytes);
            redirectAttributes.addFlashAttribute("message", "文件上传成功：" + file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "文件上传失败：" + e.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/uploadMultiple")
    public String handleMultipleFileUpload(@RequestParam("files") MultipartFile[] files,
                                           RedirectAttributes redirectAttributes) {
        boolean allUploaded = true;
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get(UPLOAD_DIR + "/" + file.getOriginalFilename());
                Files.write(path, bytes);
            } catch (IOException e) {
                e.printStackTrace();
                allUploaded = false;
            }
        }

        if (allUploaded) {
            redirectAttributes.addFlashAttribute("message", "所有文件上传成功");
        } else {
            redirectAttributes.addFlashAttribute("message", "部分文件上传失败");
        }

        return "redirect:/";
    }
}