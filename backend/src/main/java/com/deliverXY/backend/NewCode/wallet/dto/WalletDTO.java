package com.deliverXY.backend.NewCode.wallet.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletDTO {
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private Boolean isActive;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
}
