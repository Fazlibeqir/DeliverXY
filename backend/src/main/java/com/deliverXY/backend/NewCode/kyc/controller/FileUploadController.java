package com.deliverXY.backend.NewCode.kyc.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.kyc.dto.KYCBase64DTO;
import com.deliverXY.backend.NewCode.kyc.service.FileUploadService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(
            value = "/kyc",
            consumes = "multipart/form-data"
    )
    public ApiResponse<String> uploadKYC(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {

        Long userId = principal.getUser().getId();

        String url = fileUploadService.uploadKYCFile(file, documentType, userId);
        return ApiResponse.ok(url);
    }
    @PostMapping("/kyc/base64")
    public ApiResponse<String> uploadKYCBase64(
            @RequestBody KYCBase64DTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {

        Long userId = principal.getUser().getId();

        String url = fileUploadService.uploadKYCBase64(
                dto.getBase64(),
                dto.getDocumentType(),
                userId
        );

        return ApiResponse.ok(url);
    }


    @PostMapping("/profile")
    public ApiResponse<String> uploadProfile(
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {

        Long userId = principal.getUser().getId();
        String url = fileUploadService.uploadProfileImage(file, userId);

        return ApiResponse.ok(url);
    }

    @DeleteMapping
    public ApiResponse<String> deleteFile(@RequestParam String fileUrl) {
        fileUploadService.deleteFile(fileUrl);
        return ApiResponse.ok("File deleted");
    }
}
