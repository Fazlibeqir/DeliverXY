package com.deliverXY.backend.NewCode.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverStatsDTO {
    private Long totalDeliveries;
    private Long completedDeliveries;
    private Double totalEarnings;
    private Double totalDistanceKm;
    private Double averageRating;
    private Double acceptanceRate;
}
