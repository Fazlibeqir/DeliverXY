package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.notifications.repository.UserDeviceTokenRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebasePushService {

    private final UserDeviceTokenRepository tokenRepo;

    @Value("${firebase.service-account-key-path:#{null}}")
    private String serviceAccountKeyPath;

    @PostConstruct
    public void initialize() {
        try {
            // Only initialize if Firebase credentials are provided
            if (serviceAccountKeyPath != null && !serviceAccountKeyPath.isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath);
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK initialized successfully");
                }
            } else {
                log.warn("Firebase service account key path not configured. Push notifications will not be sent.");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

    public void sendToUser(Long userId, String title, String message) {

        var tokens = tokenRepo.findByUserIdAndActiveTrue(userId);

        for (var t : tokens) {
            // Use Firebase Admin SDK send()
            // Wrapped in try/catch to handle token expiration/errors
            try {
                sendPushToToken(t.getDeviceToken(), title, message);
            } catch (Exception e) {
                // Assuming a specific exception for invalid tokens (e.g., from Firebase SDK)
                if (e.getMessage().contains("InvalidRegistrationToken") || 
                    e.getMessage().contains("registration-token-not-registered")) {
                    log.warn("Invalid token found, deactivating: {}", t.getDeviceToken());
                    deactivateToken(t.getDeviceToken());
                } else {
                    log.error("Error sending push to token {}: {}", t.getDeviceToken(), e.getMessage());
                }
            }
        }
    }

    /**
     * Sends a push notification to a specific device token using Firebase Cloud Messaging.
     * 
     * @param token The FCM device token
     * @param title Notification title
     * @param body Notification body
     * @throws Exception if Firebase is not initialized or sending fails
     */
    private void sendPushToToken(String token, String title, String body) throws Exception {
        // Check if Firebase is initialized
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

    // NEW: Method to deactivate expired tokens
    @Transactional
    public void deactivateToken(String token) {
        tokenRepo.findByDeviceToken(token).forEach(t -> {
            t.setActive(false);
            tokenRepo.save(t);
        });
    }
}

