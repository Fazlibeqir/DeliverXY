package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.enums.NotificationType;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.Notification;
import com.deliverXY.backend.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createNotification(AppUser user, String title, String message,
                                   NotificationType type, String referenceId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);

        notification = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        sendRealtimeNotification(user.getId(), notification);

    }

    public void sendDeliveryRequest(AppUser driver, Delivery delivery) {
        String message = String.format("New delivery request! Pickup from %s",
                delivery.getPickupAddress());

        createNotification(
                driver,
                "New Delivery Request",
                message,
                NotificationType.DELIVERY_REQUEST,
                delivery.getId().toString()
        );
    }

    public void sendDeliveryAccepted(AppUser customer, Delivery delivery) {
        createNotification(
                customer,
                "Delivery Accepted",
                "Your delivery has been accepted by a driver",
                NotificationType.DELIVERY_ACCEPTED,
                delivery.getId().toString()
        );
    }

    public void sendDeliveryStarted(AppUser customer, Delivery delivery) {
        createNotification(
                customer,
                "Delivery Started",
                "Your driver is on the way to pickup location",
                NotificationType.DELIVERY_STARTED,
                delivery.getId().toString()
        );
    }

    public void sendDeliveryCompleted(AppUser customer, Delivery delivery) {
        createNotification(
                customer,
                "Delivery Completed",
                "Your delivery has been completed successfully",
                NotificationType.DELIVERY_COMPLETED,
                delivery.getId().toString()
        );
    }

    private void sendRealtimeNotification(Long userId, Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );
            log.info("Sent real-time notification to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send real-time notification to user {}: {}", userId, e.getMessage());
        }
    }

    public List<Notification> getUserNotifications(AppUser user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(AppUser user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(AppUser user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notificationRepository.delete(notification);
            log.info("Deleted notification {}", notificationId);
        });
    }
}