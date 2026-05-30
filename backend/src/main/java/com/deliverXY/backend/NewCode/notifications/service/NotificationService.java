package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.common.enums.NotificationType;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.notifications.domain.Notification;
import com.deliverXY.backend.NewCode.notifications.dto.NotificationDTO;
import com.deliverXY.backend.NewCode.notifications.repository.NotificationRepository;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final NotificationPersistenceService persistenceService;

    @Transactional
    public Notification create(
            AppUser user,
            String title,
            String message,
            NotificationType type,
            String refId
    ) {
        return persistenceService.persist(user, title, message, type, refId);
    }

    @Transactional
    public void sendDeliveryRequest(AppUser driver, Delivery delivery) {
        persistenceService.persist(
                driver,
                "New Delivery Request",
                "Pickup at " + delivery.getPickupAddress(),
                NotificationType.DELIVERY_REQUEST,
                delivery.getId().toString()
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getForUser(AppUser user) {
        return repo.findByUserOrderByCreatedAtDesc(Objects.requireNonNull(user, "user"), PageRequest.of(0, 50))
                .stream()
                .map(NotificationDTO::from)
                .toList();
    }

    @Transactional
    public void markRead(Long id) {
        Long notificationId = Objects.requireNonNull(id, "id");
        repo.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            repo.save(n);
        });
    }
}
