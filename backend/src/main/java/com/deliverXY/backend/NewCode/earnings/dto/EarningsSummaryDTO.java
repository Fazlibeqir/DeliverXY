package com.deliverXY.backend.NewCode.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class EarningsSummaryDTO {
    private BigDecimal totalEarned;
    private BigDecimal totalTips;
    private Long totalDeliveries;
    private double totalDistanceKm;
}
