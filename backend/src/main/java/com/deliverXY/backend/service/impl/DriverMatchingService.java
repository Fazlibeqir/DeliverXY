package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.constants.DeliveryConstants;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.DriverLocation;
import com.deliverXY.backend.repositories.DriverLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverMatchingService {

    private final DriverLocationRepository driverLocationRepository;
    private final GeolocationService geolocationService;
    private final NotificationService notificationService;

    /**
     * Find and assign the nearest available driver to a delivery
     */
    @Transactional
    public Optional<AppUser> findNearestDriver(Double pickupLat, Double pickupLon) {
        log.info("Searching for drivers near location: {}, {}", pickupLat, pickupLon);

        double currentRadius = DeliveryConstants.INITIAL_SEARCH_RADIUS_KM;

        while (currentRadius <= DeliveryConstants.MAX_SEARCH_RADIUS_KM) {
            List<DriverLocation> nearbyDrivers = driverLocationRepository
                    .findNearbyDrivers(pickupLat, pickupLon, currentRadius);

            if (!nearbyDrivers.isEmpty()) {
                DriverLocation closestDriver = nearbyDrivers.get(0);
                double distance = geolocationService.calculateDistance(
                        pickupLat, pickupLon,
                        closestDriver.getLatitude(),
                        closestDriver.getLongitude()
                );
                log.info("Found driver {} at distance {} km", closestDriver.getDriver().getId(), distance);
                return Optional.of(closestDriver.getDriver());
            }

            currentRadius += DeliveryConstants.SEARCH_RADIUS_INCREMENT_KM;
            log.debug("No drivers found, expanding search to {} km", currentRadius);
        }

        log.warn("No available drivers found within {} km", DeliveryConstants.MAX_SEARCH_RADIUS_KM);
        return Optional.empty();
    }

    /**
     * Broadcast delivery request to multiple nearby drivers
     */
    @Transactional
    public void broadcastDeliveryRequest(Delivery delivery, Double pickupLat, Double pickupLon) {
        List<DriverLocation> nearbyDrivers = driverLocationRepository
                .findNearbyDrivers(pickupLat, pickupLon, DeliveryConstants.INITIAL_SEARCH_RADIUS_KM);

        log.info("Broadcasting delivery {} to {} nearby drivers", delivery.getId(), nearbyDrivers.size());

        nearbyDrivers.forEach(driverLocation ->
                notificationService.sendDeliveryRequest(driverLocation.getDriver(), delivery)
        );
    }

    /**
     * Calculate estimated time for driver to reach pickup location
     */
    public int calculateDriverETA(Long driverId, Double pickupLat, Double pickupLon) {
        return driverLocationRepository.findByDriverId(driverId)
                .map(loc -> {
                    double distance = geolocationService.calculateDistance(
                            loc.getLatitude(), loc.getLongitude(), pickupLat, pickupLon
                    );
                    return geolocationService.calculateETA(distance, DeliveryConstants.DEFAULT_SPEED_KMH);
                })
                .orElse(0);
    }
}