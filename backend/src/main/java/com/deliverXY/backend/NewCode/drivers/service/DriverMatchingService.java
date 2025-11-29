package com.deliverXY.backend.NewCode.drivers.service;

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

        double radius = 1.5; // km initial
        double maxRadius = 10;

        while (radius <= maxRadius) {

            var drivers = locationRepo.findNearbyDrivers(lat, lon, radius);

            if (!drivers.isEmpty()) {
                return Optional.of(drivers.get(0).getDriver());
            }

            radius += 1.0;
        }

        return Optional.empty();
    }

    public void broadcastDeliveryRequest(Delivery d) {

        var drivers = locationRepo.findNearbyDrivers(
                d.getPickupLatitude(),
                d.getPickupLongitude(),
                3.0
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
                    return locationService.calculateETA(dist, 35);
                })
                .orElse(0);
    }
}
