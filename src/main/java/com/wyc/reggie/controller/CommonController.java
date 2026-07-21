package com.wyc.reggie.controller;

import com.wyc.reggie.common.AppException;
import com.wyc.reggie.common.R;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

// 上传下载文件控制器
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //1:创建变量保存上传图片路径
        String basePath = "D:\\work\\reggie\\upload\\";
        //2：获取原始文件名        abc.png
        String oldFileName = file.getOriginalFilename();
        //3：获取原始文件名中后缀   1.png
        int idx = oldFileName.lastIndexOf(".");  //1
        String suffix = oldFileName.substring(idx); //.png
        //4:创建新文件名
        String newFileName = UUID.randomUUID() + suffix;
        //5:创建一个目录对象（上传图片路径）
        File dir = new File(basePath);
        //6:如果目录不存在创建
        if (!dir.exists()) {
            dir.mkdirs();//创建
        }
        //7：将临时上传文件转存新目录中  12312.tmp 1d99d9d.png
        try {
            file.transferTo(new File(basePath + newFileName));
        } catch (IOException e) {
            log.info(e.getMessage());
            throw new AppException("上传文件失败");
        }
        return R.success(newFileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {

        String basePath = "D:\\work\\reggie\\upload\\";
        try {
            //1:准备读取文件对象，准备发送客户对象
            FileInputStream fis = new FileInputStream(
                    new File(basePath + name));
            ServletOutputStream outputStream = response.getOutputStream();
            //2:指定发送客户图片类型 image/jpeg
            response.setContentType("image/jpeg");
            //3:读取源图片/向客户端发送数据   1024
            int len = 0;   //实际上一次读取多少数 1.jpg 1025 [1024][1]
            byte[] bytes = new byte[1024];
            while ((len = fis.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            //4:关闭文件对象，关闭发送客户对象
            outputStream.close();
            fis.close();
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new AppException("文件下载失败");
        }
    }
}
