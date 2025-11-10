package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    @Value("${file.upload.max-size:10485760}") // 10MB default
    private long maxFileSize;
    
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "image/gif"
    };
    
    private static final String[] ALLOWED_DOCUMENT_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "application/pdf"
    };
    
    @Override
    public String uploadKYCFile(MultipartFile file, String documentType, Long userId) throws IOException {
        if (!isValidFile(file)) {
            throw new IllegalArgumentException("Invalid file");
        }
        
        // Create directory structure
        String userDir = uploadPath + "/kyc/" + userId;
        Path userPath = Paths.get(userDir);
        Files.createDirectories(userPath);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = documentType + "_" + UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = userPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for storage in database
        return "/uploads/kyc/" + userId + "/" + filename;
    }
    
    @Override
    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file");
        }
        
        // Create directory structure
        String userDir = uploadPath + "/profiles/" + userId;
        Path userPath = Paths.get(userDir);
        Files.createDirectories(userPath);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = "profile_" + UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = userPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for storage in database
        return "/uploads/profiles/" + userId + "/" + filename;
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                String relativePath = fileUrl.substring(1); // Remove leading slash
                Path filePath = Paths.get(uploadPath, relativePath);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete file: " + fileUrl + ", Error: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isValidFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        for (String allowedType : ALLOWED_DOCUMENT_TYPES) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean isValidFileSize(MultipartFile file) {
        if (file == null) {
            return false;
        }
        
        return file.getSize() <= maxFileSize;
    }
    
    private boolean isValidFile(MultipartFile file) {
        return isValidFileType(file) && isValidFileSize(file);
    }
    
    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        
        return false;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
} 