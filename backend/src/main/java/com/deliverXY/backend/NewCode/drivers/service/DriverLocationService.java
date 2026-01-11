package com.deliverXY.backend.NewCode.drivers.service;

import com.deliverXY.backend.NewCode.drivers.domain.DriverLocation;

public interface DriverLocationService {
    DriverLocation updateLocation(Long driverId, Double lat, Double lon);
}