package com.deliverXY.backend.NewCode.deliveries.dto;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import lombok.Data;
import java.math.BigDecimal;
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


    //TODO the lat and long for pickup and dropoff better to get them somehow in map maybe idk

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

    private PaymentProvider paymentProvider;
    private String promoCode;
    // Timing
    private LocalDateTime requestedPickupTime;
    private LocalDateTime requestedDeliveryTime;

    
    // Client ID (will be set from authenticated user)
    private Long clientId;
    
    public DeliveryDTO() {
    }
} 