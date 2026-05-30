package com.deliverXY.backend.NewCode.kyc.service;

import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

@Service
public class MultipartFileMetadataService {

    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    public record ValidatedMetadata(String contentType, String fileExtension) {
    }

    public ValidatedMetadata validate(MultipartFile file, String[] allowedContentTypes) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        byte[] header = readHeader(file, 8);
        String detectedType = detectContentType(header);
        if (!isAllowed(detectedType, allowedContentTypes)) {
            throw new BadRequestException("Unsupported file content type");
        }
        if (!matchesMagic(header, detectedType)) {
            throw new BadRequestException("File content does not match declared type");
        }
        return new ValidatedMetadata(detectedType, extensionFor(detectedType));
    }

    public boolean matchesDeclaredType(MultipartFile file, String contentType) throws IOException {
        ValidatedMetadata metadata = validate(file, new String[]{contentType});
        return metadata.contentType().equals(contentType);
    }

    private static byte[] readHeader(MultipartFile file, int length) throws IOException {
        byte[] header = new byte[length];
        try (InputStream input = file.getInputStream()) {
            int read = input.read(header);
            if (read < length) {
                throw new BadRequestException("File is too small");
            }
        }
        return header;
    }

    private static String detectContentType(byte[] header) {
        if (startsWith(header, PDF_MAGIC)) {
            return "application/pdf";
        }
        if (startsWith(header, PNG_MAGIC)) {
            return "image/png";
        }
        if (startsWith(header, JPEG_MAGIC)) {
            return "image/jpeg";
        }
        throw new BadRequestException("Unsupported file content type");
    }

    private static boolean matchesMagic(byte[] header, String contentType) {
        return switch (contentType) {
            case "application/pdf" -> startsWith(header, PDF_MAGIC);
            case "image/png" -> startsWith(header, PNG_MAGIC);
            case "image/jpeg", "image/jpg" -> startsWith(header, JPEG_MAGIC);
            default -> false;
        };
    }

    private static boolean isAllowed(String detectedType, String[] allowedContentTypes) {
        return Arrays.stream(allowedContentTypes)
                .anyMatch(allowed -> allowed.equalsIgnoreCase(detectedType));
    }

    private static String extensionFor(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            default -> throw new BadRequestException("Unsupported file extension");
        };
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
