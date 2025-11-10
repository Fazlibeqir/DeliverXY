package com.deliverXY.backend.controllers;

import com.deliverXY.backend.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }
    
    @PostMapping("/kyc")
    public ResponseEntity<?> uploadKYCDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam("userId") Long userId) {

        return handleFileUpload(file, () -> fileUploadService.uploadKYCFile(file, documentType, userId));
    }
    
    @PostMapping("/profile")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {

        return handleFileUpload(file, () -> fileUploadService.uploadProfileImage(file, userId));
    }
    
    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        try {
            fileUploadService.deleteFile(fileUrl);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return internalError("Failed to delete file: " + e.getMessage());
        }
    }
    
    @GetMapping("/validate")
    public ResponseEntity<String> validateFile(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) return badRequest("File is empty");
        if (!fileUploadService.isValidFileType(file)) return badRequest("Invalid file type");
        if (!fileUploadService.isValidFileSize(file)) return badRequest("File size too large");
        return ResponseEntity.ok("File is valid");
    }

    private ResponseEntity<?> handleFileUpload(MultipartFile file, UploadOperation operation){
        try {
            if (file.isEmpty()) return badRequest("File is empty");
            if (!fileUploadService.isValidFileType(file)) return badRequest("Invalid file type");
            if (!fileUploadService.isValidFileSize(file)) return badRequest("File size too large");

            String fileUrl = operation.execute();
            return ResponseEntity.ok(fileUrl);

        } catch (IOException e) {
            return internalError("Failed to upload file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }
    private ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    private ResponseEntity<String> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
    @FunctionalInterface
    private interface UploadOperation{
        String execute() throws IOException;
    }
} 