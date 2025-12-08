package com.deliverXY.backend.NewCode.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidatePromoCodeRequest {
    private String promoCode;
    private BigDecimal orderAmount;
}