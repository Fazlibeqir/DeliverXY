package com.deliverXY.backend.NewCode.deliveries.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FareBreakdown {

    private double totalFare;
    private double baseFare;
    private double distanceFare;
    private double timeFare;

    private double distanceKm;
    private int estimatedMinutes;

    private String currency;

    private double surgeMultiplier;

    private double cityCenterCharge;
    private double airportCharge;
}
