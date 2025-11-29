package com.deliverXY.backend.NewCode.user.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.domain.AppUserAgentProfile;
import com.deliverXY.backend.NewCode.user.domain.AppUserLocation;
import com.deliverXY.backend.NewCode.user.dto.AgentStatusUpdateDTO;
import com.deliverXY.backend.NewCode.user.service.AgentLocationService;
import com.deliverXY.backend.NewCode.user.service.AgentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {
    private final AgentProfileService profileService;
    private final AgentLocationService locationService;

    @GetMapping("/profile")
    public ApiResponse<AppUserAgentProfile> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(profileService.getProfile(principal.getUser().getId()));
    }

    @PutMapping("/profile")
    public ApiResponse<AppUserAgentProfile> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AppUserAgentProfile profile
    ) {
        return ApiResponse.ok(profileService.updateProfile(principal.getUser().getId(), profile));
    }

    @PutMapping("/location")
    public ApiResponse<AppUserLocation> updateLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AgentStatusUpdateDTO status
    ) {
        return ApiResponse.ok(
                locationService.updateLocation(
                        principal.getUser().getId(),
                        status.getCurrentLatitude(),
                        status.getCurrentLongitude()
                )
        );
    }
}
