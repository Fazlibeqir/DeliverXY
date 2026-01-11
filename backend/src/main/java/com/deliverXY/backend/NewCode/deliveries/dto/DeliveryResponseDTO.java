package com.deliverXY.backend.NewCode.deliveries.dto;

import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.common.enums.PaymentMethod;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeliveryResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String packageType;
    private Double packageWeight;
    private String packageDimensions;
    
    // Pickup Information
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupContactName;
    private String pickupContactPhone;
    private String pickupInstructions;
    
    // Dropoff Information
    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private String dropoffContactName;
    private String dropoffContactPhone;
    private String dropoffInstructions;
    
    // Timing
    private LocalDateTime requestedPickupTime;
    private LocalDateTime requestedDeliveryTime;
    private LocalDateTime actualPickupTime;
    private LocalDateTime actualDeliveryTime;
    private LocalDateTime expiresAt;
    
    // Status and Assignment
    private DeliveryStatus status;
    private Long clientId;
    private String clientUsername;
    private String clientEmail;
    private Long agentId;
    private String agentUsername;
    private String agentEmail;
    private LocalDateTime assignedAt;
    
    // Payment Information
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;
    
    // Tracking and Communication
    private Double estimatedDistance;
    private Integer estimatedDuration;
    private String trackingCode;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String cancelledBy;
} 