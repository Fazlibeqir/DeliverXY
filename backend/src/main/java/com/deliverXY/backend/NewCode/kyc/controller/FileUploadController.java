package com.deliverXY.backend.NewCode.kyc.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.kyc.service.FileUploadService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/kyc")
    public ApiResponse<String> uploadKYC(
            @RequestParam MultipartFile file,
            @RequestParam String documentType,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {

        Long userId = principal.getUser().getId();

        String url = fileUploadService.uploadKYCFile(file, documentType, userId);
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
