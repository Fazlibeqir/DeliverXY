package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.notifications.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebasePushService {

    private final UserDeviceTokenRepository tokenRepo;

    public void sendToUser(Long userId, String title, String message) {

        var tokens = tokenRepo.findByUserId(userId);

        for (var t : tokens) {
            // Use Firebase Admin SDK send()
            // I'll generate this method once you confirm Firebase config
            sendPushToToken(t.getDeviceToken(), title, message);
        }
    }
    private void sendPushToToken(String token, String title, String body) {
        // Firebase Admin SDK code will be added when you give me your Firebase setup
    }
}

