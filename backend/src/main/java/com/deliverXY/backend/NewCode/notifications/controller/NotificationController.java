package com.deliverXY.backend.NewCode.notifications.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.notifications.service.NotificationService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<?> all(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(notificationService.getForUser(principal.getUser()));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<String> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResponse.ok("Marked as read");
    }
}
