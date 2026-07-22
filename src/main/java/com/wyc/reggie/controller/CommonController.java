package com.wyc.reggie.controller;

import com.wyc.reggie.common.AppException;
import com.wyc.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.upload.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("上传文件不能为空");
        }

        String oldFileName = file.getOriginalFilename();
        if (oldFileName == null || !oldFileName.contains(".")) {
            throw new AppException("上传文件格式不正确");
        }

        String suffix = oldFileName.substring(oldFileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + suffix;

        File dir = new File(basePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new AppException("创建上传目录失败");
        }

        try {
            // 使用 absoluteFile 规避某些容器下的相对路径隐患
            file.transferTo(new File(dir, newFileName).getAbsoluteFile());
        } catch (IOException e) {
            log.error("上传文件失败: {}", e.getMessage());
            throw new AppException("上传文件失败");
        }
        return R.success(newFileName);
    }

    /** 默认占位图，当请求的图片文件不存在时返回 */
    private static final Resource PLACEHOLDER = new ClassPathResource("backend/images/noImg.png");

    @GetMapping("/download")
    public ResponseEntity<Resource> download(String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // 1. 安全校验：解析规范化路径，防目录穿越 (Path Traversal)
        Path basePathPath = Paths.get(basePath).toAbsolutePath().normalize();
        Path filePath = basePathPath.resolve(name).normalize();

        if (!filePath.startsWith(basePathPath)) {
            log.warn("检测到非法文件路径访问请求: {}", name);
            throw new AppException("非法文件名");
        }

        // 2. 尝试加载实际文件，不存在则降级为占位图
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.debug("文件缺失，返回占位图: {}", filePath.getFileName());
                resource = PLACEHOLDER;
            }
        } catch (MalformedURLException e) {
            resource = PLACEHOLDER;
        }

        // 3. 动态获取 Media Type
        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            contentType = null;
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
