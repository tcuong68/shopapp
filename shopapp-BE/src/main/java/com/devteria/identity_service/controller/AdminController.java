package com.devteria.identity_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class AdminController {
    @GetMapping("/test-file")
    public ResponseEntity<String> testFile() {
        File file = new File("D:/Code/Java Code/shopapp-BE/src/main/uploads/799f3f04-7b9b-4217-8317-59aac4a9cbd2_quandai.jpg");
        if (file.exists()) {
            return ResponseEntity.ok("✅ File tồn tại và đọc được.");
        } else {
            return ResponseEntity.status(404).body("❌ File không tồn tại hoặc sai đường dẫn.");
        }
    }
}
