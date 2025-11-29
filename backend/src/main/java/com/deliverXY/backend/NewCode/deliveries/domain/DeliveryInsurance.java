package com.deliverXY.backend.NewCode.deliveries.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_insurance")
@Data
@NoArgsConstructor
public class DeliveryInsurance {

    @Id
    private Long deliveryId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private Boolean isInsured = false;
    private BigDecimal insuranceAmount = BigDecimal.ZERO;
    private BigDecimal insurancePremium = BigDecimal.ZERO;
}
