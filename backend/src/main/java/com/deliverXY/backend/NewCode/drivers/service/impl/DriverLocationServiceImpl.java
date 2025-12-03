package com.deliverXY.backend.NewCode.drivers.service.impl;

import com.deliverXY.backend.NewCode.drivers.domain.DriverLocation;
import com.deliverXY.backend.NewCode.drivers.repository.DriverLocationRepository;
import com.deliverXY.backend.NewCode.drivers.service.DriverLocationService;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverLocationServiceImpl implements DriverLocationService {

    private final DriverLocationRepository locationRepo;
    private final AppUserRepository appUserRepo; // To load the driver user

    @Override
    @Transactional
    public DriverLocation updateLocation(Long driverId, Double lat, Double lon) {
        // 1. Load the driver user to ensure they exist
        var driver = appUserRepo.findById(driverId)
                .orElseThrow(() -> new NotFoundException("Driver not found")); // Use proper exception

        // 2. Load existing location or create a new one
        DriverLocation location = locationRepo.findById(driverId)
                .orElse(new DriverLocation());

        // 3. Set properties
        location.setDriver(driver);
        location.setLatitude(lat);
        location.setLongitude(lon);

        // 4. Save (PrePersist/PreUpdate hooks will handle the timestamp)
        return locationRepo.save(location);
    }
}
