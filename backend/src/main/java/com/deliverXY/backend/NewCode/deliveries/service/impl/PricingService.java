package com.deliverXY.backend.NewCode.deliveries.service.impl;

import com.deliverXY.backend.NewCode.common.constants.DeliveryConstants;
import com.deliverXY.backend.NewCode.common.constants.TimeConstants;
import com.deliverXY.backend.NewCode.deliveries.service.PricingConfigService;
import com.deliverXY.backend.NewCode.payments.service.PromoCodeService;
import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    // -------------------------------------------------------------
    // MAIN FARE CALCULATION
    // -------------------------------------------------------------
    public double calculateFare(double distanceKm, int estimatedMinutes) {

        PricingConfig config = pricingConfigService.getActivePricing(DEFAULT_CITY);

        double baseFare = config.getBaseFare();
        double distanceFare = distanceKm * config.getPerKmRate();
        double timeFare = estimatedMinutes * config.getPerMinuteRate();

        double surge = getSurgeMultiplier(config);
        double total = (baseFare + distanceFare + timeFare) * surge;

        total = Math.max(total, config.getMinimumFare());

        return round(total);
    }

    // -------------------------------------------------------------
    // COORDINATE-BASED
    // -------------------------------------------------------------
    public double calculateFareFromCoordinates(double pickupLat, double pickupLon,
                                               double dropoffLat, double dropoffLon) {

        PricingConfig config = pricingConfigService.getActivePricing(DEFAULT_CITY);

        double distance = geolocationService.distanceKm(pickupLat, pickupLon, dropoffLat, dropoffLon);
        int mins = geolocationService.calculateETA(distance, DeliveryConstants.AVERAGE_CITY_SPEED_KMH);

        double fare = calculateFare(distance, mins);
        fare = applyZoneCharges(fare, pickupLat, pickupLon, dropoffLat, dropoffLon, config);

        return round(fare);
    }

    // -------------------------------------------------------------
    // ZONE CHARGES
    // -------------------------------------------------------------
    private double applyZoneCharges(double fare, double pickupLat, double pickupLon,
                                    double dropoffLat, double dropoffLon,
                                    PricingConfig config) {

        if (isInCityCenter(pickupLat, pickupLon) || isInCityCenter(dropoffLat, dropoffLon)) {
            fare *= config.getCityCenterMultiplier();
        }

        if (isAtAirport(pickupLat, pickupLon) || isAtAirport(dropoffLat, dropoffLon)) {
            fare += config.getAirportSurcharge();
        }

        return fare;
    }

    // -------------------------------------------------------------
    // SURGE LOGIC BASED ON DB CONFIG
    // -------------------------------------------------------------
    private double getSurgeMultiplier(PricingConfig config) {

        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();

        if (time.isAfter(TimeConstants.NIGHT_START) || time.isBefore(TimeConstants.NIGHT_END))
            return config.getNightMultiplier();

        if (time.isAfter(TimeConstants.MORNING_PEAK_START) && time.isBefore(TimeConstants.MORNING_PEAK_END))
            return config.getPeakHourMultiplier();

        if (time.isAfter(TimeConstants.EVENING_PEAK_START) && time.isBefore(TimeConstants.EVENING_PEAK_END))
            return config.getPeakHourMultiplier();

        if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY)
            return config.getWeekendMultiplier();

        return config.getSurgeMultiplier(); // default multiplier
    }

    // -------------------------------------------------------------
    // LOCATION HELPERS
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
    // HELPERS
    // -------------------------------------------------------------
    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
