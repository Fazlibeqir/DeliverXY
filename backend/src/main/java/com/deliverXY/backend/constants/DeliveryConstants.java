
package com.deliverXY.backend.constants;

public final class DeliveryConstants {

    private DeliveryConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Pricing Constants (Skopje, North Macedonia)
    public static final String CURRENCY = "MKD";
    public static final double BASE_FARE = 50.0;
    public static final double PER_KM_RATE = 30.0;
    public static final double PER_MINUTE_RATE = 2.0;
    public static final double MINIMUM_FARE = 80.0;

    // Commission & Earnings
    public static final double PLATFORM_COMMISSION_RATE = 20.0; // 20%
    public static final double DRIVER_EARNING_RATE = 80.0; // 80%

    // Surge Pricing Multipliers
    public static final double PEAK_HOUR_MULTIPLIER = 1.3;
    public static final double NIGHT_MULTIPLIER = 1.25;
    public static final double WEEKEND_MULTIPLIER = 1.15;
    public static final double CITY_CENTER_MULTIPLIER = 1.1;

    // Fixed Charges
    public static final double AIRPORT_SURCHARGE = 100.0;
    public static final double LONG_DISTANCE_BONUS = 50.0; // Over 20km
    public static final double LONG_DISTANCE_THRESHOLD_KM = 20.0;

    // Location Constants (Skopje)
    public static final double SKOPJE_CENTER_LAT = 41.9973;
    public static final double SKOPJE_CENTER_LON = 21.4280;
    public static final double SKOPJE_AIRPORT_LAT = 41.9616;
    public static final double SKOPJE_AIRPORT_LON = 21.6214;
    public static final double CITY_CENTER_RADIUS_KM = 2.0;
    public static final double AIRPORT_RADIUS_KM = 1.0;

    // Driver Matching
    public static final double INITIAL_SEARCH_RADIUS_KM = 10.0;
    public static final double MAX_SEARCH_RADIUS_KM = 50.0;
    public static final double SEARCH_RADIUS_INCREMENT_KM = 5.0;

    // Average Speeds
    public static final double AVERAGE_CITY_SPEED_KMH = 35.0;
    public static final double DEFAULT_SPEED_KMH = 40.0;

    // Earth Radius for calculations
    public static final int EARTH_RADIUS_KM = 6371;

    // Exchange Rate (approximate)
    public static final double MKD_TO_EUR = 0.01625;
}