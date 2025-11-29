
package com.deliverXY.backend.NewCode.deliveries.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareResponseDTO {
    private Double totalFare;
    private String currency;

    private Double distanceKm;
    private Integer estimatedMinutes;

    private Double baseFare;
    private Double distanceFare;
    private Double timeFare;
    private Double surgeMultiplier;

    private Boolean cityCenterSurcharge;
    private Double cityCenterAmount;

    private Boolean airportSurcharge;
    private Double airportAmount;

    private String surgeReason; // "Peak Hour", "Night Time", "Weekend", etc.
    private Double discount;
    private String promoCodeApplied;
}