package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.constants.TimeConstants;
import com.deliverXY.backend.models.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import  com.deliverXY.backend.constants.DeliveryConstants;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private final GeolocationService geolocationService;
    private final PromoCodeService promoCodeService;


    /**
     * Calculate delivery fare based on distance and time (Skopje-specific)
     */
    public double calculateFare(double distanceKm, int estimatedMinutes) {
        double baseFare = DeliveryConstants.BASE_FARE;
        double distanceFare = distanceKm * DeliveryConstants.PER_KM_RATE;
        double timeFare = estimatedMinutes * DeliveryConstants.PER_MINUTE_RATE;

        double totalFare = baseFare + distanceFare + timeFare;

        // Apply surge pricing if during peak hours
        double surgeMultiplier = getSurgeMultiplier();
        totalFare *= surgeMultiplier;
        totalFare = Math.max(totalFare, DeliveryConstants.MINIMUM_FARE);


        log.info("Calculated fare for Skopje: Base={} MKD, Distance={}km ({} MKD), Time={}min ({} MKD), Surge={}x, Total={} MKD",
                baseFare, distanceKm, distanceFare, estimatedMinutes, timeFare, surgeMultiplier, totalFare);

        return Math.round(totalFare * 100.0) / 100.0; // Round to 2 decimal places
    }

    /**
     * Calculate fare from coordinates with Skopje-specific zone pricing
     */
    public double calculateFareFromCoordinates(double pickupLat, double pickupLon,
                                               double dropoffLat, double dropoffLon) {
        double distance = geolocationService.calculateDistance(
                pickupLat, pickupLon, dropoffLat, dropoffLon
        );
        int estimatedMinutes = geolocationService.calculateETA(distance, DeliveryConstants.AVERAGE_CITY_SPEED_KMH); // Average 35 km/h in Skopje

        double fare = calculateFare(distance, estimatedMinutes);

       fare = applyZoneCharges(fare, pickupLat, pickupLon, dropoffLat, dropoffLon);

        return Math.round(fare * 100.0) / 100.0;
    }
    /**
     * Apply zone-specific charges
     */
    private double applyZoneCharges(double fare, double pickupLat, double pickupLon,
                                    double dropoffLat, double dropoffLon) {
        // City center surcharge
        if (isInCityCenter(pickupLat, pickupLon) || isInCityCenter(dropoffLat, dropoffLon)) {
            fare *= DeliveryConstants.CITY_CENTER_MULTIPLIER;
            log.debug("City center surcharge applied: {} MKD", fare);
        }

        // Airport surcharge
        if (isAtAirport(pickupLat, pickupLon) || isAtAirport(dropoffLat, dropoffLon)) {
            fare += DeliveryConstants.AIRPORT_SURCHARGE;
            log.debug("Airport surcharge applied: +{} MKD, Total: {} MKD",
                    DeliveryConstants.AIRPORT_SURCHARGE, fare);
        }

        return fare;
    }

    /**
     * Get current surge multiplier based on Skopje time and conditions
     */
    private double getSurgeMultiplier() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        double multiplier = 1.0;

        // Night hours (23:00 - 06:00)
        if (currentTime.isAfter(TimeConstants.NIGHT_START) || currentTime.isBefore(TimeConstants.NIGHT_END)) {
            multiplier = DeliveryConstants.NIGHT_MULTIPLIER;
            log.info("Night time multiplier applied: {}x", multiplier);
            return multiplier;
        }

        // Morning peak hours (07:30 - 09:30)
        if (currentTime.isAfter(TimeConstants.MORNING_PEAK_START) && currentTime.isBefore(TimeConstants.MORNING_PEAK_END)) {
            multiplier = DeliveryConstants.PEAK_HOUR_MULTIPLIER;
            log.info("Morning peak hour multiplier applied: {}x", multiplier);
            return multiplier;
        }

        // Evening peak hours (16:30 - 19:00)
        if (currentTime.isAfter(TimeConstants.EVENING_PEAK_START) && currentTime.isBefore(TimeConstants.EVENING_PEAK_END)) {
            multiplier = DeliveryConstants.PEAK_HOUR_MULTIPLIER;
            log.info("Evening peak hour multiplier applied: {}x", multiplier);
            return multiplier;
        }

        // Weekend multiplier (Saturday and Sunday)
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            multiplier = DeliveryConstants.WEEKEND_MULTIPLIER;
            log.info("Weekend multiplier applied: {}x", multiplier);
            return multiplier;
        }

        return multiplier;
    }

    /**
     * Check if location is in Skopje city center (Centar Municipality area)
     */
    private boolean isInCityCenter(double latitude, double longitude) {

        return geolocationService.isWithinRadius(
                latitude,longitude,
                DeliveryConstants.SKOPJE_CENTER_LAT,
                DeliveryConstants.SKOPJE_CENTER_LON,
                DeliveryConstants.CITY_CENTER_RADIUS_KM
        );
    }

    /**
     * Check if location is at Skopje Airport
     */
    private boolean isAtAirport(double latitude, double longitude) {
        return geolocationService.isWithinRadius(
                latitude, longitude,
                DeliveryConstants.SKOPJE_AIRPORT_LAT,
                DeliveryConstants.SKOPJE_AIRPORT_LON,
                DeliveryConstants.AIRPORT_RADIUS_KM
        );
    }

    /**
     * Apply promo code discount
     */
    public double applyPromoCode(double originalFare, String promoCode, AppUser user) {
        PromoCodeService.PromoCodeValidationResult result =
                promoCodeService.validatePromoCode(promoCode, user, originalFare);

        if (result.isValid()) {
            log.info("Promo code '{}' applied: -{} MKD", promoCode, result.getDiscountAmount());
            return Math.max(DeliveryConstants.MINIMUM_FARE, originalFare - result.getDiscountAmount());
        }

        log.warn("Invalid promo code '{}': {}", promoCode, result.getMessage());
        return originalFare;
    }

    /**
     * Get fare estimate with breakdown
     */
    public FareBreakdown getFareBreakdown(double pickupLat, double pickupLon,
                                          double dropoffLat, double dropoffLon) {
        double distance = geolocationService.calculateDistance(
                pickupLat, pickupLon, dropoffLat, dropoffLon
        );
        int estimatedMinutes = geolocationService.calculateETA(distance, DeliveryConstants.AVERAGE_CITY_SPEED_KMH);

        double baseFare = DeliveryConstants.BASE_FARE;
        double distanceFare = distance * DeliveryConstants.PER_KM_RATE;
        double timeFare = estimatedMinutes * DeliveryConstants.PER_MINUTE_RATE;
        double surgeMultiplier = getSurgeMultiplier();

        double subtotal = (baseFare + distanceFare + timeFare) * surgeMultiplier;

        // Zone charges
        double cityCenterCharge = 0.0;
        if (isInCityCenter(pickupLat, pickupLon) || isInCityCenter(dropoffLat, dropoffLon)) {
            cityCenterCharge = subtotal * (DeliveryConstants.CITY_CENTER_MULTIPLIER - 1.0);
            subtotal *= DeliveryConstants.CITY_CENTER_MULTIPLIER;
        }

        double airportCharge = 0.0;
        if (isAtAirport(pickupLat, pickupLon) || isAtAirport(dropoffLat, dropoffLon)) {
            airportCharge = DeliveryConstants.AIRPORT_SURCHARGE;
            subtotal += DeliveryConstants.AIRPORT_SURCHARGE;
        }

        double total = Math.max(DeliveryConstants.MINIMUM_FARE, subtotal);

        return new FareBreakdown(
                baseFare,
                distanceFare,
                timeFare,
                surgeMultiplier,
                cityCenterCharge,
                airportCharge,
                total,
                distance,
                estimatedMinutes,
                DeliveryConstants.CURRENCY
        );
    }



    /**
     * Convert MKD to EUR for display (approximate rate)
     */
    public double convertToEUR(double mkd) {
        return Math.round(mkd * DeliveryConstants.MKD_TO_EUR * 100.0) / 100.0;
    }

    // Inner class for fare breakdown
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class FareBreakdown {
        private double baseFare;
        private double distanceFare;
        private double timeFare;
        private double surgeMultiplier;
        private double cityCenterCharge;
        private double airportCharge;
        private double totalFare;
        private double distanceKm;
        private int estimatedMinutes;
        private String currency;
    }
}