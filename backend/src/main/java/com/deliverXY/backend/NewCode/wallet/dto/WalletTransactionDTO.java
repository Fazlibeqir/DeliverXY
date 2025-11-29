package com.deliverXY.backend.NewCode.wallet.dto;

import com.deliverXY.backend.NewCode.common.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletTransactionDTO {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String reference;
    private LocalDateTime createdAt;
}
