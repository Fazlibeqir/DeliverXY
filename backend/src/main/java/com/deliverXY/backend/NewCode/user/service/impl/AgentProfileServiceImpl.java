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
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AgentProfileServiceImpl implements AgentProfileService {

    private final AppUserAgentProfileRepository repo;
    private final AppUserService userService;

    @Override
    public AppUserAgentProfile getProfile(Long userId) {
        Long id = Objects.requireNonNull(userId, "userId");
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Agent profile not found"));
    }

    @Override
    @Transactional
    public AppUserAgentProfile updateProfile(Long userId, AgentProfileDTO data) {
        Long id = Objects.requireNonNull(userId, "userId");
        AppUser user = userService.requireById(id);

        AppUserAgentProfile profile = repo.findById(id)
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
