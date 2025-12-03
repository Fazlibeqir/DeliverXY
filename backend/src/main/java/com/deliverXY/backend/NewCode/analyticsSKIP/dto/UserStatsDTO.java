package com.deliverXY.backend.NewCode.analyticsSKIP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStatsDTO {
    private Long totalTrips;
    private Long completedTrips;
    private Long cancelledTrips;
    private Double totalSpent;
    private Double totalDistanceKm;
    private Long favoriteDriverId;
}

