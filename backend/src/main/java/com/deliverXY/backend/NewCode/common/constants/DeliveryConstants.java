
package com.deliverXY.backend.NewCode.common.constants;

public final class DeliveryConstants {

    private DeliveryConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Location Constants (Skopje)
    public static final double[][] CITY_CENTER_POLYGON = {
            {41.99461, 21.43043},
            {41.99843, 21.42539},
            {41.99981, 21.43367},
            {41.99361, 21.43855},
            {41.99095, 21.43143}
    };
    public static final double SKOPJE_CENTER_LAT = 41.9973;
    public static final double SKOPJE_CENTER_LON = 21.4280;
    public static final double SKOPJE_AIRPORT_LAT = 41.9616;
    public static final double SKOPJE_AIRPORT_LON = 21.6214;
    public static final double CITY_CENTER_RADIUS_KM = 2.0;
    public static final double AIRPORT_RADIUS_KM = 1.0;

    // Average Speeds
    public static final double AVERAGE_CITY_SPEED_KMH = 35.0;
    public static final double DEFAULT_SPEED_KMH = 40.0;

    // Earth Radius for calculations
    public static final int EARTH_RADIUS_KM = 6371;

}