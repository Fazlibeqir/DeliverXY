package com.deliverXY.backend.NewCode.user.service.impl;

import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.domain.AppUserLocation;
import com.deliverXY.backend.NewCode.user.repository.AppUserLocationRepository;
import com.deliverXY.backend.NewCode.user.service.AgentLocationService;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AgentLocationServiceImpl implements AgentLocationService {
    private final AppUserLocationRepository repo;
    private final AppUserService userService;

    @Override
    @Transactional
    public AppUserLocation updateLocation(Long userId, Double lat, Double lon) {
        Long id = Objects.requireNonNull(userId, "userId");
        AppUser user = userService.requireById(id);

        AppUserLocation loc = repo.findById(id)
                .orElse(new AppUserLocation());

        loc.setUser(user);
        loc.setLatitude(lat);
        loc.setLongitude(lon);

        return repo.save(loc);
    }

    @Override
    public AppUserLocation getLocation(Long userId) {
        Long id = Objects.requireNonNull(userId, "userId");
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));
    }
}
