package com.deliverXY.backend.NewCode.drivers.service;

import com.deliverXY.backend.NewCode.drivers.domain.DriverLocation;
import org.springframework.lang.NonNull;

public interface DriverLocationService {
    DriverLocation updateLocation(@NonNull Long driverId, Double lat, Double lon);
}