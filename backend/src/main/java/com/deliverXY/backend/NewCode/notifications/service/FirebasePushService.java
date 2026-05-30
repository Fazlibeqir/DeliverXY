package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.notifications.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebasePushService {

    private final UserDeviceTokenRepository tokenRepo;
    private final DeviceTokenService deviceTokenService;

    public void sendToUser(Long userId, String title, String message) {
        var tokens = tokenRepo.findByUserIdAndActiveTrue(userId, PageRequest.of(0, 50)).getContent();

        for (var t : tokens) {
            try {
                sendPushToToken(t.getDeviceToken(), title, message);
            } catch (Exception e) {
                if (isInvalidRegistration(e)) {
                    log.warn("Invalid device registration for user {}, deactivating device", userId);
                    deviceTokenService.deactivateByDeviceToken(t.getDeviceToken());
                } else {
                    log.error("Error sending push notification to user {}: {}", userId, e.getMessage());
                }
            }
        }
    }

    private static boolean isInvalidRegistration(Exception e) {
        String message = e.getMessage();
        return message != null && message.contains("InvalidRegistration");
    }

    private void sendPushToToken(String deviceToken, String title, String body) throws Exception {
        // Mock implementation. Replace with actual Firebase Admin SDK code.
    }
}

