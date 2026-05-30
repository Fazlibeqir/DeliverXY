package com.deliverXY.backend.NewCode.kyc.service.impl;

import com.deliverXY.backend.NewCode.common.config.AppUploadProperties;
import com.deliverXY.backend.NewCode.common.config.FileUploadProperties;
import com.deliverXY.backend.NewCode.kyc.service.FileUploadService;
import com.deliverXY.backend.NewCode.kyc.service.MultipartFileMetadataService;
import com.deliverXY.backend.NewCode.kyc.service.MultipartFileMetadataService.ValidatedMetadata;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png"
    };

    private static final String[] ALLOWED_DOCUMENT_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    };

    private final AppUploadProperties uploadProperties;
    private final FileUploadProperties fileUploadProperties;
    private final MultipartFileMetadataService metadataService;

    @Override
    public String uploadKYCFile(MultipartFile file, String documentType, Long userId) throws IOException {
        ValidatedMetadata metadata = validateFile(file, ALLOWED_DOCUMENT_TYPES);

        Path dir = Paths.get(uploadProperties.dir(), "kyc", userId.toString());
        Files.createDirectories(dir);

        String safeDocumentType = documentType.replaceAll("[^a-zA-Z0-9_-]", "");
        String filename = safeDocumentType + "_" + storageName(metadata);
        Path filePath = dir.resolve(filename);

        saveFile(file, filePath);

        return "/uploads/kyc/" + userId + "/" + filename;
    }

    @Override
    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        ValidatedMetadata metadata = validateFile(file, ALLOWED_IMAGE_TYPES);

        Path dir = Paths.get(uploadProperties.dir(), "profiles", userId.toString());
        Files.createDirectories(dir);

        String filename = "profile_" + storageName(metadata);
        Path filePath = dir.resolve(filename);

        saveFile(file, filePath);

        return "/uploads/profiles/" + userId + "/" + filename;
    }

    @Override
    public String uploadKYCBase64(String base64, String documentType, Long userId) throws IOException {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalArgumentException("File is empty");
        }
        byte[] bytes = Base64.getDecoder().decode(base64);
        if (bytes.length > fileUploadProperties.maxSize()) {
            throw new IllegalArgumentException("File is too large");
        }
        if (!hasJpegMagic(bytes) && !hasPngMagic(bytes)) {
            throw new IllegalArgumentException("Invalid file type");
        }

        Path dir = Paths.get(uploadProperties.dir(), "kyc", userId.toString());
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
                Path path = Paths.get(uploadProperties.dir()).resolve(fileUrl.substring("/uploads/".length()));
                Files.deleteIfExists(path);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean isValidFileType(MultipartFile file) {
        return canValidate(file, ALLOWED_DOCUMENT_TYPES);
    }

    @Override
    public boolean isValidFileSize(MultipartFile file) {
        return file != null && file.getSize() <= fileUploadProperties.maxSize();
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        return canValidate(file, ALLOWED_IMAGE_TYPES);
    }

    private ValidatedMetadata validateFile(MultipartFile file, String[] allowedTypes) throws IOException {
        if (!isValidFileSize(file)) {
            throw new IllegalArgumentException("File is too large");
        }
        return metadataService.validate(file, allowedTypes);
    }

    private boolean canValidate(MultipartFile file, String[] allowedTypes) {
        try {
            validateFile(file, allowedTypes);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String storageName(ValidatedMetadata metadata) {
        return UUID.randomUUID() + metadata.fileExtension();
    }

    private void saveFile(MultipartFile file, Path path) throws IOException {
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean hasJpegMagic(byte[] bytes) {
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private static boolean hasPngMagic(byte[] bytes) {
        return bytes.length >= 4
                && bytes[0] == (byte) 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47;
    }
}
