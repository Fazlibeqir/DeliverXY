package com.deliverXY.backend.models;

import com.deliverXY.backend.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_earnings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverEarnings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private AppUser driver;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Column(name = "delivery_fare")
    private Double deliveryFare; // Total delivery price

    @Column(name = "platform_commission_rate")
    private Double platformCommissionRate = 20.0; // 20% default

    @Column(name = "platform_commission")
    private Double platformCommission;

    @Column(name = "driver_earning")
    private Double driverEarning; // Amount driver gets (fare - commission)

    @Column(name = "tips")
    private Double tips = 0.0;

    @Column(name = "bonus")
    private Double bonus = 0.0; // Incentive bonuses

    @Column(name = "total_earning")
    private Double totalEarning; // driver_earning + tips + bonus

    @Column(name = "earned_date")
    private LocalDate earnedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status")
    private PayoutStatus payoutStatus = PayoutStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_id")
    private Payout payout;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (earnedDate == null) {
            earnedDate = LocalDate.now();
        }
        calculateEarnings();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateEarnings();
    }

    private void calculateEarnings() {
        if (deliveryFare != null && platformCommissionRate != null) {
            platformCommission = deliveryFare * (platformCommissionRate / 100.0);
            driverEarning = deliveryFare - platformCommission;
            totalEarning = driverEarning + (tips != null ? tips : 0.0) + (bonus != null ? bonus : 0.0);
        }
    }

}