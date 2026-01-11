package com.deliverXY.backend.NewCode.deliveries.dto;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeliveryDTO {
    private String title;
    private String description;
    private String packageType;
    private Double packageWeight;
    private String packageDimensions;
    private Boolean isFragile;
    private Boolean isUrgent;

    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupContactName;
    private String pickupContactPhone;
    private String pickupInstructions;

    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private String dropoffContactName;
    private String dropoffContactPhone;
    private String dropoffInstructions;

    private PaymentProvider paymentProvider;
    private String promoCode;
    private LocalDateTime requestedPickupTime;
    private LocalDateTime requestedDeliveryTime;

    private Long clientId;
    
    public DeliveryDTO() {
    }
} 