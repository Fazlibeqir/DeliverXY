package com.deliverXY.backend.models;

import com.deliverXY.backend.enums.DeliveryStatus;
import com.deliverXY.backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "deliveries")
@NoArgsConstructor
@Data
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Delivery Info
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "package_type")
    private String packageType; // SMALL, MEDIUM, LARGE, FRAGILE, etc.

    @Column(name = "package_weight")
    private Double packageWeight; // in kg

    @Column(name = "package_dimensions")
    private String packageDimensions; // "LxWxH in cm"

    @Column(name = "is_fragile")
    private Boolean isFragile = false;

    @Column(name = "is_urgent")
    private Boolean isUrgent = false;

    // Pickup Information
    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "pickup_latitude")
    private Double pickupLatitude;

    @Column(name = "pickup_longitude")
    private Double pickupLongitude;

    @Column(name = "pickup_contact_name")
    private String pickupContactName;

    @Column(name = "pickup_contact_phone")
    private String pickupContactPhone;

    @Column(name = "pickup_instructions")
    private String pickupInstructions;

    // Dropoff Information
    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress;

    @Column(name = "dropoff_latitude")
    private Double dropoffLatitude;

    @Column(name = "dropoff_longitude")
    private Double dropoffLongitude;

    @Column(name = "dropoff_contact_name")
    private String dropoffContactName;

    @Column(name = "dropoff_contact_phone")
    private String dropoffContactPhone;

    @Column(name = "dropoff_instructions")
    private String dropoffInstructions;

    // Timing
    @Column(name = "requested_pickup_time")
    private LocalDateTime requestedPickupTime;

    @Column(name = "requested_delivery_time")
    private LocalDateTime requestedDeliveryTime;

    @Column(name = "actual_pickup_time")
    private LocalDateTime actualPickupTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Status and Assignment
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status = DeliveryStatus.REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AppUser client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AppUser agent;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    // Payment Information
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "urgent_fee")
    private BigDecimal urgentFee = BigDecimal.ZERO;

    @Column(name = "fragile_fee")
    private BigDecimal fragileFee = BigDecimal.ZERO;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "tip_amount")
    private BigDecimal tipAmount = BigDecimal.ZERO;

    @Column(name = "final_amount")
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method")
    private String paymentMethod; // CARD, WALLET, CASH

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    private Double tip;
    private Double driverEarnings;
    private Double driverRating;

    // Tracking and Communication
    @Column(name = "estimated_distance")
    private Double estimatedDistance; // in km

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in minutes

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "tracking_code", unique = true)
    private String trackingCode;

    // Insurance and Safety
    @Column(name = "is_insured")
    private Boolean isInsured = false;

    @Column(name = "insurance_amount")
    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    @Column(name = "insurance_premium")
    private BigDecimal insurancePremium = BigDecimal.ZERO;

    // Ratings and Reviews
    @Column(name = "client_rating")
    private Integer clientRating; // 1-5 stars

    @Column(name = "client_review")
    private String clientReview;

    @Column(name = "agent_rating")
    private Integer agentRating; // 1-5 stars

    @Column(name = "agent_review")
    private String agentReview;

    @Column(name = "reviewed_by_client")
    private Boolean reviewedByClient = false;

    @Column(name = "reviewed_by_agent")
    private Boolean reviewedByAgent = false;

    // Metadata
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancelled_by")
    private String cancelledBy; // CLIENT, AGENT, SYSTEM


    public Delivery(String title, String description, String pickupAddress, String dropoffAddress,
                   BigDecimal basePrice, AppUser client) {
        this.title = title;
        this.description = description;
        this.pickupAddress = pickupAddress;
        this.dropoffAddress = dropoffAddress;
        this.basePrice = basePrice;
        this.client = client;
        this.trackingCode = generateTrackingCode();
        calculateTotalPrice();
    }


    private String generateTrackingCode() {
        return "DX" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    private void calculateTotalPrice() {
        this.totalPrice = this.basePrice
            .add(this.urgentFee)
            .add(this.fragileFee);
        this.finalAmount = this.totalPrice.add(this.tipAmount);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

// Add these helper methods to your existing Delivery class

    /**
     * Get the driver (agent) assigned to this delivery
     */
    public AppUser getDriver() {
        return this.agent;
    }

    /**
     * Set the driver (agent) for this delivery
     */
    public void setDriver(AppUser driver) {
        this.agent = driver;
    }

    /**
     * Get the customer (client) who created this delivery
     */
    public AppUser getCustomer() {
        return this.client;
    }

    /**
     * Get the estimated distance as the actual distance
     */
    public Double getDistance() {
        return this.estimatedDistance;
    }

    /**
     * Set the distance
     */
    public void setDistance(Double distance) {
        this.estimatedDistance = distance;
    }

    /**
     * Get final price (converted from BigDecimal to Double)
     */
    public Double getFinalPrice() {
        return this.finalAmount != null ? this.finalAmount.doubleValue() : 0.0;
    }

    /**
     * Set final price (converted from Double to BigDecimal)
     */
    public void setFinalPrice(Double price) {
        this.finalAmount = price != null ? BigDecimal.valueOf(price) : BigDecimal.ZERO;
    }

    /**
     * Get customer ID
     */
    public Long getCustomerId() {
        return this.client != null ? this.client.getId() : null;
    }

    /**
     * Get driver ID
     */
    public Long getDriverId() {
        return this.agent != null ? this.agent.getId() : null;
    }

    /**
     * Check if delivery is completed
     */
    public boolean isDelivered() {
        return DeliveryStatus.DELIVERED.equals(this.status);
    }

    /**
     * Check if delivery is cancelled
     */
    public boolean isCancelled() {
        return DeliveryStatus.CANCELLED.equals(this.status);
    }
    /**
     * Check if delivery is in progress
     */
    public boolean isInProgress() {
        return this.status != null && this.status.isInProgress();
    }
}