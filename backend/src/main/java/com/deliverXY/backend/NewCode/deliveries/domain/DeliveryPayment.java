package com.deliverXY.backend.NewCode.deliveries.domain;

import com.deliverXY.backend.NewCode.common.enums.PaymentMethod;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_payment")
@Data
@NoArgsConstructor
public class DeliveryPayment {

    @Id
    private Long deliveryId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private PaymentMethod paymentMethod;

    private BigDecimal tipAmount = BigDecimal.ZERO;
    private BigDecimal finalAmount;

    private LocalDateTime paidAt;
}
