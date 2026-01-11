package com.deliverXY.backend.NewCode.user.service;

import com.deliverXY.backend.NewCode.user.domain.AppUserLocation;

public interface AgentLocationService {

    AppUserLocation updateLocation(Long userId, Double lat, Double lon);

    AppUserLocation getLocation(Long userId);
}
