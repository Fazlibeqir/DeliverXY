package com.deliverXY.backend.NewCode.user.service.impl;

import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.domain.AppUserAgentProfile;
import com.deliverXY.backend.NewCode.user.dto.AgentProfileDTO;
import com.deliverXY.backend.NewCode.user.repository.AppUserAgentProfileRepository;
import com.deliverXY.backend.NewCode.user.service.AgentProfileService;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AgentProfileServiceImpl implements AgentProfileService {

    private final AppUserAgentProfileRepository repo;
    private final AppUserService userService;

    @Override
    public AppUserAgentProfile getProfile(Long userId) {
        return repo.findById(userId).orElseThrow(() -> new NotFoundException("Agent profile not found"));
    }

    @Override
    public AppUserAgentProfile updateProfile(Long userId, AgentProfileDTO data) {
        AppUser user = userService.requireById(userId);

        AppUserAgentProfile profile = repo.findById(userId)
                .orElse(new AppUserAgentProfile());

        profile.setUser(user);
        profile.setDriversLicenseNumber(data.getDriversLicenseNumber());
        profile.setDriversLicenseExpiry(data.getDriversLicenseExpiry());
        profile.setDriversLicenseFrontUrl(data.getDriversLicenseFrontUrl());
        profile.setDriversLicenseBackUrl(data.getDriversLicenseBackUrl());

        // Added status update logic (optional)
        if (data.getIsAvailable() != null) {
            profile.setIsAvailable(data.getIsAvailable());
        }

        return repo.save(profile);
    }
}
