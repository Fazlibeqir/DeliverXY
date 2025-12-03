package com.deliverXY.backend.NewCode.earnings.domain;

import com.deliverXY.backend.NewCode.common.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_payouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long driverId;

    private BigDecimal amountPaid;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @Enumerated(EnumType.STRING) // <-- NEW: Status tracking
    private PayoutStatus status = PayoutStatus.PENDING;

    private LocalDateTime paidAt = LocalDateTime.now();

    private String transactionRef; // <-- Added for processing
    private String processedBy; // <-- Added for processing

    @PrePersist // Ensure paidAt is only set if status is PAID
    protected void onCreate() {
        if (paidAt == null && status == PayoutStatus.PAID) {
            paidAt = LocalDateTime.now();
        }
    }
}
