package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliyunOSSOUtill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliyunOSSOUtill aliyunOSSOperator;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result  accept(MultipartFile file) throws Exception {
        String URL = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
        return Result.success(URL);
    }
}
