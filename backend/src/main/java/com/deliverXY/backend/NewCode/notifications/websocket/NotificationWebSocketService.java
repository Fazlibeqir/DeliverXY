package com.deliverXY.backend.NewCode.notifications.websocket;

import com.deliverXY.backend.NewCode.notifications.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate ws;

    public void pushToUser(Long userId, NotificationDTO dto) {
        ws.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                dto
        );
    }
}

