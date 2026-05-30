package com.deliverXY.backend.NewCode.drivers.service.impl;

import com.deliverXY.backend.NewCode.common.constants.DriverConstants;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.service.impl.LocationService;
import com.deliverXY.backend.NewCode.drivers.domain.DriverLocation;
import com.deliverXY.backend.NewCode.drivers.repository.DriverLocationRepository;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverMatchingService {

    private final DriverLocationRepository locationRepo;
    private final LocationService locationService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Optional<AppUser> findNearestDriver(double lat, double lon) {
        double maxRadius = DriverConstants.MAX_SEARCH_RADIUS_KM;
        List<DriverLocation> candidates = locationRepo.findNearbyDrivers(lat, lon, maxRadius);

        double radius = DriverConstants.INITIAL_SEARCH_RADIUS_KM;
        while (radius <= maxRadius) {
            final double searchRadius = radius;
            for (DriverLocation driverLocation : candidates) {
                double distanceKm = locationService.distanceKm(
                        lat,
                        lon,
                        driverLocation.getLatitude(),
                        driverLocation.getLongitude()
                );
                if (distanceKm <= searchRadius) {
                    return Optional.of(driverLocation.getDriver());
                }
            }
            radius += DriverConstants.SEARCH_RADIUS_INCREMENT_KM;
        }

        return Optional.empty();
    }

    public void broadcastDeliveryRequest(Delivery d) {

        var drivers = locationRepo.findNearbyDrivers(
                d.getPickupLatitude(),
                d.getPickupLongitude(),
                DriverConstants.BROADCAST_RADIUS_KM
        );

        for (var dl : drivers) {
            notificationService.sendDeliveryRequest(dl.getDriver(), d);
        }
    }

    @Transactional(readOnly = true)
    public int calculateDriverETA(@NonNull Long driverId, double pickupLat, double pickupLon) {
        return locationRepo.findById(driverId)
                .map(loc -> {
                    double dist = locationService.distanceKm(
                            loc.getLatitude(),
                            loc.getLongitude(),
                            pickupLat,
                            pickupLon
                    );
                    return locationService.calculateETA(dist, DriverConstants.AVERAGE_DRIVER_SPEED_KMH);
                })
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public List<DriverLocation> listDriversInRadius(Double latitude, Double longitude, Double radius) {
            return locationRepo.findNearbyDrivers(latitude, longitude, radius);
    }
}
