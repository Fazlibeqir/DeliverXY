package com.deliverXY.backend.models;

import com.deliverXY.backend.enums.PaymentMethod;
import com.deliverXY.backend.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private AppUser driver;

    @Column(name = "payout_amount")
    private Double payoutAmount;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries;

    @Column(name = "total_earnings")
    private Double totalEarnings;

    @Column(name = "total_tips")
    private Double totalTips;

    @Column(name = "total_bonus")
    private Double totalBonus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status = PayoutStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "transaction_reference")
    private String transactionReference;

    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by")
    private String processedBy;

    @OneToMany(mappedBy = "payout")
    private List<DriverEarnings> earnings;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}