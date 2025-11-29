
package com.deliverXY.backend.NewCode.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidatePromoCodeResponse {
    private Boolean valid;
    private String message;
    private Double discountAmount;
    private Double finalAmount;
    private String promoCode;
}