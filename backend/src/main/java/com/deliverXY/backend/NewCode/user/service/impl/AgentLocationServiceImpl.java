package com.deliverXY.backend.NewCode.user.service.impl;

import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.domain.AppUserLocation;
import com.deliverXY.backend.NewCode.user.repository.AppUserLocationRepository;
import com.deliverXY.backend.NewCode.user.service.AgentLocationService;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AgentLocationServiceImpl implements AgentLocationService {
    private final AppUserLocationRepository repo;
    private final AppUserService userService;

    @Override
    public AppUserLocation updateLocation(Long userId, Double lat, Double lon) {
        AppUser user = userService.requireById(userId);

        AppUserLocation loc = repo.findById(userId)
                .orElse(new AppUserLocation());

        loc.setUser(user);
        loc.setLatitude(lat);
        loc.setLongitude(lon);

        return repo.save(loc);
    }

    @Override
    public AppUserLocation getLocation(Long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Location not found"));
    }
}
