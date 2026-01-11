package com.deliverXY.backend.NewCode.kyc.service.impl;

import com.deliverXY.backend.NewCode.kyc.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${app.uploads.dir}")
    private String uploadPath;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png"
    };

    private static final String[] ALLOWED_DOCUMENT_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    };

    @Override
    public String uploadKYCFile(MultipartFile file, String documentType, Long userId) throws IOException {
        validateFile(file, ALLOWED_DOCUMENT_TYPES);

        Path dir = Paths.get(uploadPath, "kyc", userId.toString());
        Files.createDirectories(dir);

        String safeDocumentType = documentType.replaceAll("[^a-zA-Z0-9_-]", "");

        String filename = safeDocumentType + "_" + uuidName(file);
        Path filePath = dir.resolve(filename);

        saveFile(file, filePath);

        return "/uploads/kyc/" + userId + "/" + filename;
    }

    @Override
    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        validateFile(file, ALLOWED_IMAGE_TYPES);

        Path dir = Paths.get(uploadPath, "profiles", userId.toString());
        Files.createDirectories(dir);

        String filename = "profile_" + uuidName(file);
        Path filePath = dir.resolve(filename);

        saveFile(file, filePath);

        return "/uploads/profiles/" + userId + "/" + filename;
    }

    @Override
    public String uploadKYCBase64(String base64, String documentType, Long userId) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(base64);

        Path dir = Paths.get(uploadPath, "kyc", userId.toString());
        Files.createDirectories(dir);

        String safeType = documentType.replaceAll("[^a-zA-Z0-9_-]", "");
        String filename = safeType + "_" + UUID.randomUUID() + ".jpg";

        Path filePath = dir.resolve(filename);
        Files.write(filePath, bytes);

        return "/uploads/kyc/" + userId + "/" + filename;
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                Path path = Paths.get(uploadPath).resolve(fileUrl.substring("/uploads/".length()));
                Files.deleteIfExists(path);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean isValidFileType(MultipartFile file) {
        return isOfTypes(file, ALLOWED_DOCUMENT_TYPES);
    }

    @Override
    public boolean isValidFileSize(MultipartFile file) {
        return file != null && file.getSize() <= maxFileSize;
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        return isOfTypes(file, ALLOWED_IMAGE_TYPES);
    }

    private void validateFile(MultipartFile file, String[] allowedTypes) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File is empty");

        if (!isOfTypes(file, allowedTypes))
            throw new IllegalArgumentException("Invalid file type");

        if (!isValidFileSize(file))
            throw new IllegalArgumentException("File is too large");
    }

    private void saveFile(MultipartFile file, Path path) throws IOException {
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean isOfTypes(MultipartFile file, String[] types) {
        if (file == null || file.getContentType() == null) return false;

        for (String type : types) {
            if (type.equalsIgnoreCase(file.getContentType())) return true;
        }
        return false;
    }

    private String uuidName(MultipartFile file) {
        return UUID.randomUUID() + getExtension(file.getOriginalFilename());
    }

    private String getExtension(String name) {
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf("."));
    }
}