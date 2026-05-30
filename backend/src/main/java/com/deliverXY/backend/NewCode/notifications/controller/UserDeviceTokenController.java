package com.deliverXY.backend.NewCode.notifications.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.common.util.RequestPayloadValidator;
import com.deliverXY.backend.NewCode.notifications.dto.DeviceTokenRequest;
import com.deliverXY.backend.NewCode.notifications.service.UserDeviceTokenService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-token")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserDeviceTokenController {

    private final UserDeviceTokenService tokenService;

    @PostMapping("/register")
    public ApiResponse<String> registerToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeviceTokenRequest req
    ) {
        DeviceTokenRequest body = RequestPayloadValidator.requireBody(req);
        tokenService.registerOrUpdateToken(
                principal.getUser().getId(),
                RequestPayloadValidator.requireText(body.getToken(), "token"),
                body.getPlatform()
        );
        return ApiResponse.ok("Token registered");
    }

    @DeleteMapping("/deregister")
    public ApiResponse<String> deregisterToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeviceTokenRequest req
    ) {
        DeviceTokenRequest body = RequestPayloadValidator.requireBody(req);
        tokenService.deregisterToken(
                principal.getUser().getId(),
                RequestPayloadValidator.requireText(body.getToken(), "token")
        );
        return ApiResponse.ok("Token deregistered");
    }
}
