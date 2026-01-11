package com.deliverXY.backend.NewCode.kyc.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileUploadService {
    String uploadKYCFile(MultipartFile file, String documentType, Long userId) throws IOException;
    String uploadProfileImage(MultipartFile file, Long userId) throws IOException;
    String uploadKYCBase64(String base64, String documentType, Long userId) throws IOException;
    void deleteFile(String fileUrl);
    boolean isValidFileType(MultipartFile file);
    boolean isValidFileSize(MultipartFile file);
    boolean isValidImageFile(MultipartFile file);
} 