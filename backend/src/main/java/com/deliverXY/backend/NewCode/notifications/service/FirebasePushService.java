package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.notifications.repository.UserDeviceTokenRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebasePushService {

    private final UserDeviceTokenRepository tokenRepo;
    private final DeviceTokenService deviceTokenService;

    @Value("${firebase.service-account-key-path:#{null}}")
    private String serviceAccountKeyPath;

    @PostConstruct
    public void initialize() {
        if (serviceAccountKeyPath == null || serviceAccountKeyPath.isBlank()) {
            log.warn("Firebase service account key path not configured. Push notifications will not be sent.");
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

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
        if (message == null) {
            return false;
        }
        return message.contains("InvalidRegistration")
                || message.contains("InvalidRegistrationToken")
                || message.contains("registration-token-not-registered")
                || message.contains("Requested entity was not found");
    }

    private void sendPushToToken(String token, String title, String body) throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase not initialized. Skipping push notification to token: {}", token);
            return;
        }

        Message firebaseMessage = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance().send(firebaseMessage);
        log.info("Successfully sent push notification to token: {}. Response: {}", token, response);
    }
}
