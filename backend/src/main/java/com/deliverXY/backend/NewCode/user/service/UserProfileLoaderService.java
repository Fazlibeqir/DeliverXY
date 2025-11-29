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

@Service
@RequiredArgsConstructor
public class UserProfileLoaderService {
    private final AppUserKYCRepository kycRepo;
    private final AppUserAgentProfileRepository profileRepo;
    private final AppUserLocationRepository locationRepo;
    private final AppUserStatsRepository statsRepo;

    public AppUserKYC getKYC(Long userId) {
        return kycRepo.findById(userId).orElse(null);
    }

    public AppUserAgentProfile getProfile(Long userId) {
        return profileRepo.findById(userId).orElse(null);
    }

    public AppUserLocation getLocation(Long userId) {
        return locationRepo.findById(userId).orElse(null);
    }

    public AppUserStats getStats(Long userId) {
        return statsRepo.findById(userId).orElse(null);
    }
}
