package com.deliverXY.backend.NewCode.notifications.dto;

import com.deliverXY.backend.NewCode.notifications.domain.Notification;
import lombok.Data;

@Data
public class NotificationDTO {

    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private String referenceId;
    private String createdAt;

    public static NotificationDTO from(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType().name());
        dto.setRead(n.getIsRead());
        dto.setReferenceId(n.getReferenceId());
        dto.setCreatedAt(n.getCreatedAt().toString());
        return dto;
    }
}
