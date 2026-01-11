package com.deliverXY.backend.NewCode.payments.dto;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentInitRequest {
    private Long deliveryId;
    private BigDecimal amount;
    private PaymentProvider provider;
}
