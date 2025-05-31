package com.devteria.identity_service.controller;

import com.devteria.identity_service.dto.response.ApiResponse;
import com.devteria.identity_service.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@Slf4j
public class UploadController {
    @Autowired
    UploadService uploadService;
    @PostMapping("/image")
    public ApiResponse<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "targetFolder", defaultValue = "src/main/uploads") String targetFolder
    ) {
        String savedFileName = uploadService.handleSaveFile(file, targetFolder);

        if (savedFileName.isEmpty()) {
            return ApiResponse.<String>builder()
                    .code(400)
                    .message("Upload failed")
                    .build();
        }

        String fileUrl = "/resources/images/" + targetFolder + "/" + savedFileName;

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Upload successful")
                .result(fileUrl)
                .build();
    }
}
