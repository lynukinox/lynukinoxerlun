package com.dorm.smartnote.controller;

import com.dorm.smartnote.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@CrossOrigin
public class FileController {

    // 获取当前项目运行的根目录
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件不能为空");

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs(); // 如果目录不存在则创建

            // 生成唯一文件名，防止覆盖
            String originalName = file.getOriginalFilename();
            String suffix = originalName.substring(originalName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + suffix;

            // 保存文件到本地
            File dest = new File(UPLOAD_DIR + newFileName);
            file.transferTo(dest);

            // 返回一个可以在前端访问的 URL 路径 (配合后面的 WebConfig)
            return Result.success("http://localhost:8080/uploads/" + newFileName);
        } catch (IOException e) {
            return Result.error("头像上传失败");
        }
    }
}