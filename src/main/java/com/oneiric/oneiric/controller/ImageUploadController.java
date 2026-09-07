package com.oneiric.oneiric.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpSession session) throws IOException {

        Map<String, String> response = new HashMap<>();

        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.put("error", "Not logged in");
            return ResponseEntity.status(401).body(response);
        }

        if (file.isEmpty()) {
            response.put("error", "No file provided");
            return ResponseEntity.badRequest().body(response);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("error", "Only image files are allowed");
            return ResponseEntity.badRequest().body(response);
        }

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            response.put("error", "Image too large (max 5MB)");
            return ResponseEntity.badRequest().body(response);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;

        Path userUploadPath = Paths.get(uploadDir, username);
        Files.createDirectories(userUploadPath);

        Path filePath = userUploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        String publicUrl = "/uploads/" + username + "/" + uniqueFilename;
        response.put("url", publicUrl);
        return ResponseEntity.ok(response);
    }
}