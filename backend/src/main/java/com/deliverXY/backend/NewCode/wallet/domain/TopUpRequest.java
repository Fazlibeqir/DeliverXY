package com.deliverXY.backend.NewCode.wallet.domain;

import com.deliverXY.backend.NewCode.common.enums.TopUpStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_topups")
@Data
public class TopUpRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TopUpStatus status = TopUpStatus.PENDING;

    private String provider; // "Stripe", "FakeGateway", "CPay"

    private String referenceId; // returned by gateway

    private LocalDateTime createdAt = LocalDateTime.now();
}
