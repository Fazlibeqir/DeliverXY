package com.deliverXY.backend.models;

import com.deliverXY.backend.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payment_id", unique = true, nullable = false)
    private String paymentId; // External payment ID (Stripe, etc.)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Delivery delivery;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer", "handler"})
    private AppUser payer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer", "handler"})
    private AppUser recipient;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false)
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod; // CARD, WALLET, CASH, BANK_TRANSFER
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;
    
    @Column(name = "stripe_charge_id")
    private String stripeChargeId;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "refund_amount")
    private BigDecimal refundAmount = BigDecimal.ZERO;
    
    @Column(name = "refund_reason")
    private String refundReason;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "processing_fee")
    private BigDecimal processingFee = BigDecimal.ZERO;
    
    @Column(name = "platform_fee")
    private BigDecimal platformFee = BigDecimal.ZERO;
    
    @Column(name = "net_amount")
    private BigDecimal netAmount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    // Metadata
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data
    
    public enum PaymentType {
        DELIVERY_PAYMENT,    // Client pays for delivery
        AGENT_PAYOUT,        // Platform pays agent
        WALLET_TOPUP,        // User adds money to wallet
        WALLET_WITHDRAWAL,   // User withdraws from wallet
        REFUND,              // Refund to client
        PLATFORM_FEE         // Platform fee collection
    }
    
    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED
    }
    
    public Payment(String paymentId, AppUser payer, BigDecimal amount, 
                   PaymentType paymentType, String description) {
        this.paymentId = paymentId;
        this.payer = payer;
        this.amount = amount;
        this.paymentType = paymentType;
        this.description = description;
        this.status = PaymentStatus.PENDING;
        this.netAmount = amount.subtract(processingFee).subtract(platformFee);
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 