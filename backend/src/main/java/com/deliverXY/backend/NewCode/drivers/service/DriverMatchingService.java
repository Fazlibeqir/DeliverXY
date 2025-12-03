package com.deliverXY.backend.NewCode.drivers.service;

import com.deliverXY.backend.NewCode.common.constants.DriverConstants;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.service.impl.LocationService;
import com.deliverXY.backend.NewCode.drivers.repository.DriverLocationRepository;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class DriverMatchingService {

    private final DriverLocationRepository locationRepo;
    private final LocationService locationService;
    private final NotificationService notificationService;

    public Optional<AppUser> findNearestDriver(double lat, double lon) {

        double radius = DriverConstants.INITIAL_SEARCH_RADIUS_KM; // km initial
        double maxRadius = DriverConstants.MAX_SEARCH_RADIUS_KM;

        while (radius <= maxRadius) {

            var drivers = locationRepo.findNearbyDrivers(lat, lon, radius);

            if (!drivers.isEmpty()) {
                return Optional.of(drivers.get(0).getDriver());
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

    public int calculateDriverETA(Long driverId, double pickupLat, double pickupLon) {
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
}
