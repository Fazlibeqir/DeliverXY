package com.deliverXY.backend.NewCode.user.service;

import com.deliverXY.backend.NewCode.user.domain.AppUserAgentProfile;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.user.domain.AppUserLocation;
import com.deliverXY.backend.NewCode.user.domain.AppUserStats;
import com.deliverXY.backend.NewCode.user.repository.AppUserAgentProfileRepository;
import com.deliverXY.backend.NewCode.kyc.repository.AppUserKYCRepository;
import com.deliverXY.backend.NewCode.user.repository.AppUserLocationRepository;
import com.deliverXY.backend.NewCode.user.repository.AppUserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserProfileLoaderService {
    private final AppUserKYCRepository kycRepo;
    private final AppUserAgentProfileRepository profileRepo;
    private final AppUserLocationRepository locationRepo;
    private final AppUserStatsRepository statsRepo;

    public AppUserKYC getKYC(Long userId) {
        Long id = Objects.requireNonNull(userId, "userId");
        return kycRepo.findById(id).orElse(null);
    }

    public AppUserAgentProfile getProfile(Long userId) {
        Long id = Objects.requireNonNull(userId, "userId");
        return profileRepo.findById(id).orElse(null);
    }

    public AppUserLocation getLocation(Long userId) {
        Long id = Objects.requireNonNull(userId, "userId");
        return locationRepo.findById(id).orElse(null);
    }

    public AppUserStats getStats(Long userId) {
        Long id = Objects.requireNonNull(userId, "userId");
        return statsRepo.findById(id).orElse(null);
    }
}
