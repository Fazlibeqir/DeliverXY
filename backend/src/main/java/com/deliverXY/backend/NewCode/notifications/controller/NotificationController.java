package com.deliverXY.backend.NewCode.notifications.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.notifications.dto.NotificationDTO;
import com.deliverXY.backend.NewCode.notifications.repository.NotificationRepository;
import com.deliverXY.backend.NewCode.notifications.service.NotificationService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repo;
    private final NotificationService notificationService;
    private final AppUserService userService;

    @GetMapping
    public ApiResponse<?> all(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userService.findById(principal.getUser().getId())
                .orElseThrow(()->new NotFoundException("User not found"));
        return ApiResponse.ok(
                repo.findByUserOrderByCreatedAtDesc(user)
                        .stream().map(NotificationDTO::from).toList()
        );
    }

    @PostMapping("/{id}/read")
    public ApiResponse<String> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResponse.ok("Marked as read");
    }
}

