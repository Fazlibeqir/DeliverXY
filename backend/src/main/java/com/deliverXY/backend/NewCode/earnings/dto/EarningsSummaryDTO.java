package com.deliverXY.backend.NewCode.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EarningsSummaryDTO {
    private double totalEarned;
    private double totalTips;
    private double totalDeliveries;
    private double totalDistanceKm;
}
