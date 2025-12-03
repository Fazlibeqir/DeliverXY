package com.deliverXY.backend.NewCode.deliveries.service.impl;


import com.deliverXY.backend.NewCode.common.constants.DeliveryConstants;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private static final double EARTH_RADIUS_KM = DeliveryConstants.EARTH_RADIUS_KM;

    // Skopje City Center Polygon
    private static final double[][] CITY_CENTER_POLYGON = DeliveryConstants.CITY_CENTER_POLYGON;

    // Airport
    private static final double AIRPORT_LAT = DeliveryConstants.SKOPJE_AIRPORT_LAT;
    private static final double AIRPORT_LON = DeliveryConstants.SKOPJE_AIRPORT_LON;
    private static final double AIRPORT_RADIUS_KM = DeliveryConstants.AIRPORT_RADIUS_KM; // 1km

    /**
     * HAVERSINE distance
     */
    public double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * ETA based on average city speed
     */
    public int calculateETA(double distanceKm, double speedKmh) {
        if (speedKmh <= 0) speedKmh = DeliveryConstants.AVERAGE_CITY_SPEED_KMH;
        return (int) Math.ceil((distanceKm / speedKmh) * 60);
    }

    /**
     * Radius check
     */
    public boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusKm) {
        return distanceKm(lat1, lon1, lat2, lon2) <= radiusKm;
    }

    /**
     * Polygon check for city center
     */
    public boolean isInCityCenter(double lat, double lon) {
        boolean inside = false;
        int j = CITY_CENTER_POLYGON.length - 1;

        for (int i = 0; i < CITY_CENTER_POLYGON.length; i++) {
            double latI = CITY_CENTER_POLYGON[i][0];
            double lonI = CITY_CENTER_POLYGON[i][1];
            double latJ = CITY_CENTER_POLYGON[j][0];
            double lonJ = CITY_CENTER_POLYGON[j][1];

            if ((lonI > lon) != (lonJ > lon) &&
                    lat < ((latJ - latI) * (lon - lonI) / (lonJ - lonI) + latI)) {
                inside = !inside;
            }
            j = i;
        }
        return inside;
    }

    /**
     * Airport region check
     */
    public boolean isAtAirport(double lat, double lon) {
        return distanceKm(lat, lon, AIRPORT_LAT, AIRPORT_LON) <= AIRPORT_RADIUS_KM;
    }
}
