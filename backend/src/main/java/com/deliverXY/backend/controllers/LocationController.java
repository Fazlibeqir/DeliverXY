
package com.deliverXY.backend.controllers;

import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.DriverLocation;
import com.deliverXY.backend.models.dto.LocationUpdateDTO;
import com.deliverXY.backend.repositories.DriverLocationRepository;
import com.deliverXY.backend.service.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final DriverLocationRepository driverLocationRepository;
    private final AppUserService appUserService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Update driver location (REST endpoint)
     */
    @PutMapping("/driver/update")
    public ResponseEntity<?> updateDriverLocation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody LocationUpdateDTO locationUpdate) {

        try {
            AppUser driver = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            DriverLocation driverLocation = driverLocationRepository
                    .findByDriver(driver)
                    .orElse(new DriverLocation());

            driverLocation.setDriver(driver);
            driverLocation.setLatitude(locationUpdate.getLatitude());
            driverLocation.setLongitude(locationUpdate.getLongitude());
            driverLocation.setHeading(locationUpdate.getHeading());
            driverLocation.setSpeed(locationUpdate.getSpeed());
            driverLocation.setIsAvailable(locationUpdate.getIsAvailable());

            driverLocation = driverLocationRepository.save(driverLocation);

            // Broadcast location update to clients tracking this driver
            broadcastLocationUpdate(driver.getId(), driverLocation);

            return ResponseEntity.ok(driverLocation);

        } catch (Exception e) {
            log.error("Error updating driver location: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update location");
        }
    }

    /**
     * Update driver location via WebSocket
     */
    @MessageMapping("/location/update")
    public void updateLocationWebSocket(@Payload LocationUpdateDTO locationUpdate) {
        log.info("Received location update via WebSocket: {}", locationUpdate);
        // Process location update
        // This will be called when client sends message to /app/location/update
    }

    /**
     * Get driver current location
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getDriverLocation(@PathVariable Long driverId) {
        Optional<DriverLocation> location = driverLocationRepository.findByDriverId(driverId);

        if (location.isPresent()) {
            return ResponseEntity.ok(location.get());
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Get nearby available drivers
     */
    @GetMapping("/drivers/nearby")
    public ResponseEntity<?> getNearbyDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {

        List<DriverLocation> nearbyDrivers = driverLocationRepository
                .findNearbyDrivers(latitude, longitude, radiusKm);

        return ResponseEntity.ok(nearbyDrivers);
    }

    /**
     * Toggle driver availability
     */
    @PutMapping("/driver/availability")
    public ResponseEntity<?> toggleAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Boolean isAvailable) {

        try {
            AppUser driver = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            DriverLocation driverLocation = driverLocationRepository
                    .findByDriver(driver)
                    .orElseThrow(() -> new RuntimeException("Driver location not found"));

            driverLocation.setIsAvailable(isAvailable);
            driverLocationRepository.save(driverLocation);

            return ResponseEntity.ok("Availability updated to: " + isAvailable);

        } catch (Exception e) {
            log.error("Error updating availability: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update availability");
        }
    }

    /**
     * Broadcast location update to subscribers
     */
    private void broadcastLocationUpdate(Long driverId, DriverLocation location) {
        messagingTemplate.convertAndSend(
                "/topic/driver-location/" + driverId,
                location
        );
    }
}