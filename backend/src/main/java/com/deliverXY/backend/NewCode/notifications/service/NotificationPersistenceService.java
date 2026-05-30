package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.common.enums.NotificationType;
import com.deliverXY.backend.NewCode.notifications.domain.Notification;
import com.deliverXY.backend.NewCode.notifications.dto.NotificationDTO;
import com.deliverXY.backend.NewCode.notifications.repository.NotificationRepository;
import com.deliverXY.backend.NewCode.notifications.websocket.NotificationWebSocketService;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPersistenceService {

    private final NotificationRepository repo;
    private final NotificationWebSocketService ws;
    private final FirebasePushService firebasePush;

    @Transactional
    public Notification persist(
            AppUser user,
            String title,
            String message,
            NotificationType type,
            String refId
    ) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(refId);
        repo.save(notification);

        ws.pushToUser(user.getId(), NotificationDTO.from(notification));
        firebasePush.sendToUser(user.getId(), title, message);

        return notification;
    }
}
