package com.deliverXY.backend.NewCode.deliveries.service.impl;

import com.deliverXY.backend.NewCode.common.constants.DeliveryConstants;
import com.deliverXY.backend.NewCode.common.constants.TimeConstants;
import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;
import com.deliverXY.backend.NewCode.deliveries.dto.FareBreakdown; // Assume this DTO now uses BigDecimal for currency fields
import com.deliverXY.backend.NewCode.deliveries.service.PricingConfigService;
import com.deliverXY.backend.NewCode.payments.service.PromoCodeService;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private final LocationService geolocationService;
    private final PromoCodeService promoCodeService;
    private final PricingConfigService pricingConfigService;

    private static final String DEFAULT_CITY = "Skopje";
    private static final int SCALE = 2; // For currency

    // -------------------------------------------------------------
    // MAIN FARE BREAKDOWN (REPLACES calculateFare and calculateFareFromCoordinates)
    // -------------------------------------------------------------
    /**
     * Calculates the full fare breakdown using BigDecimal for accuracy.
     */
    public FareBreakdown getFareBreakdown(double pickupLat, double pickupLon,
                                          double dropoffLat, double dropoffLon) {

        PricingConfig config = pricingConfigService.getActivePricing(DEFAULT_CITY);

        double distanceKm = geolocationService.distanceKm(pickupLat, pickupLon, dropoffLat, dropoffLon);
        int estimatedMinutes = geolocationService.calculateETA(distanceKm, DeliveryConstants.AVERAGE_CITY_SPEED_KMH);

        // Convert configuration rates to BigDecimal
        BigDecimal baseFare = BigDecimal.valueOf(config.getBaseFare());
        BigDecimal perKmRate = BigDecimal.valueOf(config.getPerKmRate());
        BigDecimal perMinuteRate = BigDecimal.valueOf(config.getPerMinuteRate());
        BigDecimal minimumFare = BigDecimal.valueOf(config.getMinimumFare());

        // Calculate Distance and Time Fares
        BigDecimal distanceDec = BigDecimal.valueOf(distanceKm);
        BigDecimal minutesDec = BigDecimal.valueOf(estimatedMinutes);

        BigDecimal distanceFare = distanceDec.multiply(perKmRate).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal timeFare = minutesDec.multiply(perMinuteRate).setScale(SCALE, RoundingMode.HALF_UP);

        // Calculate Surge Multiplier
        BigDecimal surgeMultiplier = getSurgeMultiplier(config);

        // Base Calculation: (Base + Distance + Time) * Surge
        BigDecimal preSurgeTotal = baseFare.add(distanceFare).add(timeFare);
        BigDecimal afterSurge = preSurgeTotal
                .multiply(surgeMultiplier)
                .setScale(SCALE, RoundingMode.HALF_UP);

        boolean inCityCenter = isPickupOrDropoffInCityCenter(pickupLat, pickupLon, dropoffLat, dropoffLon);
        BigDecimal cityCenterMultiplier = getCityCenterMultiplier(config, pickupLat, pickupLon, dropoffLat, dropoffLon);

        BigDecimal cityCenterCharge = BigDecimal.ZERO;
        if (inCityCenter && cityCenterMultiplier.compareTo(BigDecimal.ONE) > 0) {
            cityCenterCharge = afterSurge
                    .multiply(cityCenterMultiplier.subtract(BigDecimal.ONE))
                    .setScale(SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal airportSurcharge = getAirportSurcharge(config, pickupLat, pickupLon, dropoffLat, dropoffLon)
                .setScale(SCALE, RoundingMode.HALF_UP);

        // Total = afterSurge + cityCenterCharge + airportSurcharge
        BigDecimal total = afterSurge
                .add(cityCenterCharge)
                .add(airportSurcharge)
                .setScale(SCALE, RoundingMode.HALF_UP);

        // Minimum fare
        total = total.max(minimumFare).setScale(SCALE, RoundingMode.HALF_UP);

        return new FareBreakdown(
                total,
                baseFare,
                distanceFare,
                timeFare,
                distanceKm,
                estimatedMinutes,
                config.getCurrency(),
                surgeMultiplier.doubleValue(),
                cityCenterCharge,
                airportSurcharge
        );
    }

    /**
     * Applies the promo code discount to the total fare.
     */
    public BigDecimal applyPromoCode(BigDecimal totalFare, String promoCode, AppUser user) {
        // Implementation depends on PromoCodeService, assuming a BigDecimal return
        if (totalFare == null) throw new IllegalArgumentException("totalFare is required");
        if (promoCode == null || promoCode.isBlank()) return totalFare;
        return promoCodeService.applyPromoCode(totalFare, promoCode, user)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    // -------------------------------------------------------------
    // ZONE CHARGE HELPERS
    // -------------------------------------------------------------
    private BigDecimal getCityCenterMultiplier(PricingConfig config, double pickupLat, double pickupLon,
                                               double dropoffLat, double dropoffLon) {
        if (isPickupOrDropoffInCityCenter(pickupLat, pickupLon, dropoffLat, dropoffLon)) {
            return BigDecimal.valueOf(config.getCityCenterMultiplier());
        }
        return BigDecimal.ONE; // Return 1 (no multiplication effect)
    }

    private BigDecimal getAirportSurcharge(PricingConfig config, double pickupLat, double pickupLon,
                                           double dropoffLat, double dropoffLon) {
        if (isPickupOrDropoffAtAirport(pickupLat, pickupLon, dropoffLat, dropoffLon)) {
            return BigDecimal.valueOf(config.getAirportSurcharge());
        }
        return BigDecimal.ZERO;
    }

    private boolean isPickupOrDropoffInCityCenter(double pickupLat, double pickupLon,
                                                  double dropoffLat, double dropoffLon) {
        // Re-implementing the original logic delegation
        return isInCityCenter(pickupLat, pickupLon) || isInCityCenter(dropoffLat, dropoffLon);
    }

    private boolean isPickupOrDropoffAtAirport(double pickupLat, double pickupLon,
                                               double dropoffLat, double dropoffLon) {
        // Re-implementing the original logic delegation
        return isAtAirport(pickupLat, pickupLon) || isAtAirport(dropoffLat, dropoffLon);
    }

    // -------------------------------------------------------------
    // SURGE LOGIC
    // -------------------------------------------------------------
    private BigDecimal getSurgeMultiplier(PricingConfig config) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();

        // NOTE: NIGHT_START/NIGHT_END requires careful comparison across midnight.
        // The original logic handles this by checking isAfter OR isBefore.
        if (time.isAfter(TimeConstants.NIGHT_START) || time.isBefore(TimeConstants.NIGHT_END))
            return BigDecimal.valueOf(config.getNightMultiplier());

        if ((time.isAfter(TimeConstants.MORNING_PEAK_START) && time.isBefore(TimeConstants.MORNING_PEAK_END)) ||
                (time.isAfter(TimeConstants.EVENING_PEAK_START) && time.isBefore(TimeConstants.EVENING_PEAK_END)))
            return BigDecimal.valueOf(config.getPeakHourMultiplier());

        if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY)
            return BigDecimal.valueOf(config.getWeekendMultiplier());

        return BigDecimal.valueOf(config.getSurgeMultiplier());
    }

    // -------------------------------------------------------------
    // REDUNDANT METHODS REMOVED: calculateFare, calculateFareFromCoordinates, applyZoneCharges.
    // -------------------------------------------------------------

    // -------------------------------------------------------------
    // LOCATION HELPERS (Kept for delegation consistency)
    // -------------------------------------------------------------
    private boolean isInCityCenter(double lat, double lon) {
        return geolocationService.isWithinRadius(
                lat, lon,
                DeliveryConstants.SKOPJE_CENTER_LAT,
                DeliveryConstants.SKOPJE_CENTER_LON,
                DeliveryConstants.CITY_CENTER_RADIUS_KM
        );
    }

    private boolean isAtAirport(double lat, double lon) {
        return geolocationService.isWithinRadius(
                lat, lon,
                DeliveryConstants.SKOPJE_AIRPORT_LAT,
                DeliveryConstants.SKOPJE_AIRPORT_LON,
                DeliveryConstants.AIRPORT_RADIUS_KM
        );
    }

    // -------------------------------------------------------------
    // HELPERS (Removed double round, only keeping BigDecimal round if needed)
    // -------------------------------------------------------------
    // The previous `round(double val)` method is removed as we primarily use BigDecimal.
}