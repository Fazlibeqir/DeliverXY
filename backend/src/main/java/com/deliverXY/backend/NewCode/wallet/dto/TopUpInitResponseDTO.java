package com.deliverXY.backend.NewCode.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopUpInitResponseDTO {

    private Long topUpId;
    private BigDecimal amount;

    // Stripe only (null for MOCK / other providers)
    private String clientSecret;

    private String provider;
}

