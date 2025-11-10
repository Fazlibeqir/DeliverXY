
package com.deliverXY.backend.controllers;

import com.deliverXY.backend.exception.ResourceNotFoundException;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Notification;
import com.deliverXY.backend.service.AppUserService;
import com.deliverXY.backend.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final AppUserService appUserService;

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser user = getCurrentUser(userDetails);

        List<Notification> notifications = notificationService.getUserNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser user = getCurrentUser(userDetails);

        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        return ResponseEntity.ok(notifications);
    }
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = getCurrentUser(userDetails);
        long count = notificationService.getUnreadNotifications(user).size();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = getCurrentUser(userDetails);

        notificationService.markAllAsRead(user);
        return ResponseEntity.ok("All notifications marked as read");
    }
    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        log.info("Notification {} deleted", id);
        return ResponseEntity.ok("Notification deleted");
    }
    /**
     * Helper method to get current user
     */
    private AppUser getCurrentUser(UserDetails userDetails) {
        return appUserService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }
}