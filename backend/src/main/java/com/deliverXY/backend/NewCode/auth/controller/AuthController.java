package com.deliverXY.backend.NewCode.auth.controller;

import com.deliverXY.backend.NewCode.auth.dto.AuthResponseDTO;
import com.deliverXY.backend.NewCode.auth.dto.LoginRequest;
import com.deliverXY.backend.NewCode.auth.dto.RefreshTokenRequest;
import com.deliverXY.backend.NewCode.auth.dto.RegisterRequest;
import com.deliverXY.backend.NewCode.auth.service.AuthService;
import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.dto.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @GetMapping("/me")
    public ApiResponse<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(authService.getCurrentUser(principal));
    }


    @PostMapping("/register")
    public ApiResponse<AuthResponseDTO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }


    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }


    @PostMapping("/refresh")
    public ApiResponse<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }
}
