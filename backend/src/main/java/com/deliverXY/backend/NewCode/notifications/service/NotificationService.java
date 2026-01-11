package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.common.enums.NotificationType;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
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
public class NotificationService {

    private final NotificationRepository repo;
    private final NotificationWebSocketService ws;
    private final FirebasePushService firebasePush;

    @Transactional
    public Notification create(
            AppUser user,
            String title,
            String message,
            NotificationType type,
            String refId
    ) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setReferenceId(refId);
        repo.save(n);

        // real-time websockets
        ws.pushToUser(user.getId(), NotificationDTO.from(n));

        // firebase push
        firebasePush.sendToUser(user.getId(), title, message);

        return n;
    }

    public void sendDeliveryRequest(AppUser driver, Delivery delivery) {
        create(
                driver,
                "New Delivery Request",
                "Pickup at " + delivery.getPickupAddress(),
                NotificationType.DELIVERY_REQUEST,
                delivery.getId().toString()
        );
    }
    @Transactional
    public void markRead(Long id) {
        repo.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            repo.save(n);
        });
    }
}
