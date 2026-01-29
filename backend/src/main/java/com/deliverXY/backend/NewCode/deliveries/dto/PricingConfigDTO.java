package com.deliverXY.backend.NewCode.deliveries.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingConfigDTO {

    private Long id;
    private String name;
    private String city;
    private String currency;
    private Double baseFare;
    private Double perKmRate;
    private Double perMinuteRate;
    private Double minimumFare;
    private Double surgeMultiplier;
    private Double cityCenterMultiplier;
    private Double airportSurcharge;
    private Double nightMultiplier;
    private Double weekendMultiplier;
    private Double peakHourMultiplier;
    private Boolean isActive;
    private Double platformCommissionPercent; // e.g. 20.0 = 20% platform, 80% driver
    private String description;
}
