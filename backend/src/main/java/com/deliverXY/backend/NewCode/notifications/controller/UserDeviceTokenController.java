package com.deliverXY.backend.NewCode.notifications.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.notifications.service.UserDeviceTokenService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/device-token")
@RequiredArgsConstructor
public class UserDeviceTokenController {

    private final UserDeviceTokenService tokenService;

    @PostMapping("/register")
    public ApiResponse<String> registerToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> req
    ) {
        String token = req.get("token");
        String platform = req.getOrDefault("platform", "ANDROID");

        if (token == null || token.isBlank()) {
            return ApiResponse.error("Token cannot be empty",400);
        }
        tokenService.registerOrUpdateToken(principal.getUser().getId(), token, platform);

        return ApiResponse.ok("Token registered");
    }
    @DeleteMapping("/deregister")
    public ApiResponse<String> deregisterToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> req
    ) {
        String token = req.get("token");

        if (token == null || token.isBlank()) {
            return ApiResponse.error("Token is required", 400);
        }

        tokenService.deregisterToken(principal.getUser().getId(), token);
        return ApiResponse.ok("Token deregistered");
    }
}

