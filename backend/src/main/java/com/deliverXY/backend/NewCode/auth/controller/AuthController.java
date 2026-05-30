package com.deliverXY.backend.NewCode.auth.controller;

import com.deliverXY.backend.NewCode.auth.dto.AuthResponseDTO;
import com.deliverXY.backend.NewCode.auth.dto.LoginRequest;
import com.deliverXY.backend.NewCode.auth.dto.RefreshTokenRequest;
import com.deliverXY.backend.NewCode.auth.dto.RegisterRequest;
import com.deliverXY.backend.NewCode.auth.service.AuthService;
import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.common.util.RequestPayloadValidator;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.dto.UserResponseDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(authService.getCurrentUser(principal));
    }

    @PermitAll
    @PreAuthorize("@publicEndpointPolicy.allowPublicAccess()")
    @PostMapping("/register")
    public ApiResponse<AuthResponseDTO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(RequestPayloadValidator.requireBody(request)));
    }

    @PermitAll
    @PreAuthorize("@publicEndpointPolicy.allowPublicAccess()")
    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(RequestPayloadValidator.requireBody(request)));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader != null && principal != null) {
            authService.logout(authHeader, principal);
        }
        return ApiResponse.ok("Logged out successfully");
    }

    @PermitAll
    @PreAuthorize("@publicEndpointPolicy.allowPublicAccess()")
    @PostMapping("/refresh")
    public ApiResponse<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(RequestPayloadValidator.requireBody(request)));
    }
}
