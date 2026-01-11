package com.deliverXY.backend.NewCode.wallet.dto;

import com.deliverXY.backend.NewCode.common.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletTransactionDTO {
    private Long id;
    private TransactionType type;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
    private String reference;
    private LocalDateTime createdAt;
}
