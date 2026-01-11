package com.deliverXY.backend.NewCode.payments.domain;

import com.deliverXY.backend.NewCode.common.enums.PaymentMethod;
import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** --------------------------------------------------
     *  RELATIONSHIPS
     *  -------------------------------------------------- */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    /**
     * Paid by client (delivery creator)
     * FUTURE: could support split payments
     */
    @Column(name = "payer_id")
    private Long payerId;

    /** --------------------------------------------------
     *  PAYMENT METHOD & PROVIDER
     *  -------------------------------------------------- */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method; // WALLET, CASH, CREDIT_CARD, PAYPAL, BANK_TRANSFER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider; // STRIPE, CPAY, WALLET, CASH, PAYPAL_API

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /** --------------------------------------------------
     *  AMOUNTS
     *  -------------------------------------------------- */
    @Column(nullable = false)
    private BigDecimal amount; // delivery finalAmount

    private BigDecimal tip;

    private BigDecimal platformFee; // system earns this

    private BigDecimal driverAmount; // goes to DriverEarnings

    private BigDecimal refundedAmount;

    /** --------------------------------------------------
     *  PROVIDER REFERENCES
     *  -------------------------------------------------- */
    @Column(unique = true)
    private String providerReference;
    // e.g. Stripe PaymentIntent ID, CPay transaction ID

    private String providerSessionId;
    // e.g. Stripe Checkout Session ID

    private String providerChargeId;
    // e.g. Stripe charge ID

    /** --------------------------------------------------
     *  TIMESTAMPS
     *  -------------------------------------------------- */
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime refundedAt;

    @Column(name = "escrow_released", nullable = false)
    private Boolean escrowReleased = false;

    @Column(name = "escrow_released_at")
    private LocalDateTime escrowReleasedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = PaymentStatus.PENDING;
        if (tip == null) tip = BigDecimal.ZERO;
        if (platformFee == null) platformFee = BigDecimal.ZERO;
        if (driverAmount == null) driverAmount = BigDecimal.ZERO;
        if (refundedAmount == null) refundedAmount = BigDecimal.ZERO;
        if (escrowReleased == null) escrowReleased = false;
        if (initiatedAt == null) initiatedAt = LocalDateTime.now();
    }
}
