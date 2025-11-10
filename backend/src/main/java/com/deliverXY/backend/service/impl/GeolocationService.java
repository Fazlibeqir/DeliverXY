package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.constants.DeliveryConstants;
import org.springframework.stereotype.Service;

@Service
public class GeolocationService {

    /**
     * Calculate distance between two points using Haversine formula
     * @return distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return DeliveryConstants.EARTH_RADIUS_KM * c;
    }

    /**
     * Calculate estimated time of arrival based on distance
     * @param distanceKm distance in kilometers
     * @param averageSpeedKmh average speed in km/h
     * @return ETA in minutes
     */
    public int calculateETA(double distanceKm, double averageSpeedKmh) {
        if (averageSpeedKmh <= 0) {
            averageSpeedKmh = DeliveryConstants.DEFAULT_SPEED_KMH;
        }
        return (int) Math.ceil((distanceKm / averageSpeedKmh) * 60);
    }

    /**
     * Check if a point is within a certain radius
     */
    public boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusKm) {
        return calculateDistance(lat1, lon1, lat2, lon2) <= radiusKm;
    }
}