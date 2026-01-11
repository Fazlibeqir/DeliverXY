package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.notifications.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebasePushService {

    private final UserDeviceTokenRepository tokenRepo;

    public void sendToUser(Long userId, String title, String message) {

        var tokens = tokenRepo.findByUserIdAndActiveTrue(userId);

        for (var t : tokens) {
            // Use Firebase Admin SDK send()
            // Wrapped in try/catch to handle token expiration/errors
            try {
                sendPushToToken(t.getDeviceToken(), title, message);
            } catch (Exception e) {
                // Assuming a specific exception for invalid tokens (e.g., from Firebase SDK)
                if (e.getMessage().contains("InvalidRegistrationToken")) {
                    log.warn("Invalid token found, deactivating: {}", t.getDeviceToken());
                    deactivateToken(t.getDeviceToken());
                } else {
                    log.error("Error sending push to token {}: {}", t.getDeviceToken(), e.getMessage());
                }
            }
        }
    }
    // Placeholder method for Firebase Admin SDK call
    private void sendPushToToken(String token, String title, String body) throws Exception {
        // Mock implementation. Replace with actual Firebase Admin SDK code.
        // if (token.equals("expired-token")) throw new RuntimeException("InvalidRegistrationToken");
        // System.out.println("Pushing to token: " + token + " | Title: " + title);
    }
    // NEW: Method to deactivate expired tokens
    @Transactional
    public void deactivateToken(String token) {
        tokenRepo.findByDeviceToken(token).forEach(t -> {
            t.setActive(false);
            tokenRepo.save(t);
        });
    }
}

