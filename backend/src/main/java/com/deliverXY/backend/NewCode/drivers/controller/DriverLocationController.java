package com.deliverXY.backend.NewCode.drivers.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.drivers.domain.DriverLocation;
import com.deliverXY.backend.NewCode.drivers.dto.DriverLocationUpdateDTO;
import com.deliverXY.backend.NewCode.drivers.service.DriverLocationService;
import com.deliverXY.backend.NewCode.drivers.service.impl.DriverMatchingService;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverLocationController {

    private final DriverLocationService driverLocationService;
    private final DriverMatchingService driverMatchingService;

    // --- DRIVER-FACING ENDPOINT ---

    /**
     * Endpoint used by the mobile app to push the driver's current location.
     * The driver's ID is taken from the authenticated user principal.
     */
    @PostMapping("/location/update")
    public ApiResponse<DriverLocation> updateLocation(
            @Valid @RequestBody DriverLocationUpdateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // We assume the authenticated user is the driver
        Long driverId = principal.getUser().getId();

        DriverLocation updatedLoc = driverLocationService.updateLocation(
                driverId,
                dto.getLatitude(),
                dto.getLongitude()
        );
        return ApiResponse.ok(updatedLoc);
    }

    // --- CLIENT/SYSTEM-FACING ENDPOINT ---

    /**
     * Endpoint used to find the single nearest available driver for assignment.
     * This calls the matching logic (which iterates radius bands).
     */
    @GetMapping("/nearby/nearest")
    public ApiResponse<AppUser> findNearestDriver(
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        AppUser nearestDriver = driverMatchingService.findNearestDriver(latitude, longitude)
                .orElseThrow(() -> new NotFoundException("No drivers found nearby")); // Use proper exception

        return ApiResponse.ok(nearestDriver);
    }

    // An endpoint to find ALL nearby locations (mainly for monitoring/internal use)
    @GetMapping("/nearby/list")
    public ApiResponse<List<DriverLocation>> listNearbyDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radius
    ) {
        // Directly call the repository or a wrapper method in DriverMatchingService
        var locations = driverMatchingService.listDriversInRadius(latitude, longitude, radius);
        return ApiResponse.ok(locations);
    }
}