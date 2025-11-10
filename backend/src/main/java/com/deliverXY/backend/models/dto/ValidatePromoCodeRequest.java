package com.deliverXY.backend.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidatePromoCodeRequest {
    private String promoCode;
    private Double orderAmount;
}