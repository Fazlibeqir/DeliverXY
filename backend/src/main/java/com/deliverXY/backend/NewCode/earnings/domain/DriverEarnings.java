package com.deliverXY.backend.NewCode.earnings.domain;

import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_earnings")
@Data
@NoArgsConstructor
public class DriverEarnings {

    @Id
    private Long deliveryId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private Long agentId; // Stored for fast reporting

    private BigDecimal driverEarnings = BigDecimal.ZERO;
    private BigDecimal tip = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();

}
