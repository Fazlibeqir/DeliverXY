package com.deliverXY.backend.NewCode.notifications.websocket;

import com.deliverXY.backend.NewCode.notifications.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate ws;

    private static final String NOTIFICATIONS_DESTINATION = "/queue/notifications";

    public void pushToUser(Long userId, NotificationDTO dto) {
        Long id = Objects.requireNonNull(userId, "userId");
        String userKey = Objects.requireNonNull(String.valueOf(id), "userKey");
        NotificationDTO payload = Objects.requireNonNull(dto, "dto");
        ws.convertAndSendToUser(userKey, NOTIFICATIONS_DESTINATION, payload);
    }
}

