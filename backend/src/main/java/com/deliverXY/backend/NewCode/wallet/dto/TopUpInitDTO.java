package com.deliverXY.backend.NewCode.wallet.dto;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpInitDTO {
    private BigDecimal amount;
    private PaymentProvider provider;
}