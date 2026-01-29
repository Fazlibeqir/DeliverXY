
package com.deliverXY.backend.NewCode.deliveries.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pricing_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Standard Skopje", "Express", "Heavy Package"

    @Column(nullable = false)
    private String city = "Skopje";

    @Column(nullable = false)
    private String currency = "MKD";

    @Column(name = "base_fare")
    private Double baseFare = 50.0; // Base fare in MKD

    @Column(name = "per_km_rate")
    private Double perKmRate = 30.0; // Per km rate in MKD

    @Column(name = "per_minute_rate")
    private Double perMinuteRate = 2.0; // Per minute rate in MKD

    @Column(name = "minimum_fare")
    private Double minimumFare = 80.0; // Minimum fare in MKD

    @Column(name = "surge_multiplier")
    private Double surgeMultiplier = 1.0;

    @Column(name = "city_center_multiplier")
    private Double cityCenterMultiplier = 1.1; // 10% extra for city center

    @Column(name = "airport_surcharge")
    private Double airportSurcharge = 100.0; // Fixed airport charge in MKD

    @Column(name = "night_multiplier")
    private Double nightMultiplier = 1.25; // 25% extra for night (23:00-06:00)

    @Column(name = "weekend_multiplier")
    private Double weekendMultiplier = 1.15; // 15% extra on weekends

    @Column(name = "peak_hour_multiplier")
    private Double peakHourMultiplier = 1.3; // 30% extra during rush hour

    @Column(name = "is_active")
    private Boolean isActive = true;

    /** Platform commission as percentage of delivery fare (e.g. 20.0 = 20%). Driver gets (100 - this). */
    @Column(name = "platform_commission_percent")
    private Double platformCommissionPercent = 20.0;

    private String description; // e.g., "Standard delivery in Skopje"
}