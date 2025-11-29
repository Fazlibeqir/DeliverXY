package com.deliverXY.backend.NewCode.earnings.domain;

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

    private LocalDateTime paidAt = LocalDateTime.now();
}
