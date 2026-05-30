package com.deliverXY.backend.NewCode.kyc.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.kyc.dto.KYCBase64DTO;
import com.deliverXY.backend.NewCode.kyc.service.FileUploadService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(
            value = "/kyc",
            consumes = "multipart/form-data"
    )
    public ApiResponse<String> uploadKYC(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("documentType") @NotBlank String documentType,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {

        Long userId = principal.getUser().getId();

        String url = fileUploadService.uploadKYCFile(file, documentType, userId);
        return ApiResponse.ok(url);
    }

    @PostMapping("/kyc/base64")
    public ApiResponse<String> uploadKYCBase64(
            @Valid @RequestBody KYCBase64DTO dto,
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
            @RequestParam("file") @NotNull MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {

        Long userId = principal.getUser().getId();
        String url = fileUploadService.uploadProfileImage(file, userId);

        return ApiResponse.ok(url);
    }

    @DeleteMapping
    public ApiResponse<String> deleteFile(
            @RequestParam String fileUrl,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        fileUploadService.deleteFile(fileUrl);
        return ApiResponse.ok("File deleted");
    }
}
