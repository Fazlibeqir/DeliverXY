package com.deliverXY.backend.NewCode.deliveries.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FareBreakdown {

    private BigDecimal totalFare;
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal timeFare;

    private double distanceKm;
    private int estimatedMinutes;

    private String currency;

    private double surgeMultiplier;

    private BigDecimal cityCenterCharge;
    private BigDecimal airportCharge;
}
