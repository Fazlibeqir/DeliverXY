package com.deliverXY.backend.NewCode.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DriverPayoutDTO {
    private Long id;
    private BigDecimal amount;
    private String periodStart;
    private String periodEnd;
    private String paidAt;
    private String status;
}

