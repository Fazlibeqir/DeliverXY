package com.deliverXY.backend.NewCode.payments.dto;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import lombok.Data;

@Data
public class PaymentInitRequest {
    private Long deliveryId;
    private PaymentProvider provider;
}
