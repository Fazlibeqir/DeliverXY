package com.deliverXY.backend.NewCode.analyticsSKIP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlatformStatsDTO {
    private Integer totalDeliveries;
    private Integer completedDeliveries;
    private Double totalRevenue;
    private Double totalDistanceKm;
    private Long totalUsers;
    private Long activeDrivers;
    private Long activeCustomers;
}
