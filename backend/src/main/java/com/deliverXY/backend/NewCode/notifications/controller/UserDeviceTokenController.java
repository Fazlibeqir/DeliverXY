package com.deliverXY.backend.NewCode.notifications.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.notifications.domain.UserDeviceToken;
import com.deliverXY.backend.NewCode.notifications.repository.UserDeviceTokenRepository;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/device-token")
@RequiredArgsConstructor
public class UserDeviceTokenController {

    private final UserDeviceTokenRepository tokenRepo;
    private final AppUserService userService;

    @PostMapping("/register")
    public ApiResponse<String> registerToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> req
    ) {
        String token = req.get("token");
        String platform = req.getOrDefault("platform", "ANDROID");

        var user = userService.findById(principal.getUser().getId()).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        UserDeviceToken entity = new UserDeviceToken();
        entity.setUser(user);
        entity.setDeviceToken(token);
        entity.setPlatform(platform);

        tokenRepo.save(entity);

        return ApiResponse.ok("Token registered");
    }
}

