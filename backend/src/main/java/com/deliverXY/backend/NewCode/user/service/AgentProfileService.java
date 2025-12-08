package com.deliverXY.backend.NewCode.user.service;

import com.deliverXY.backend.NewCode.user.domain.AppUserAgentProfile;
import com.deliverXY.backend.NewCode.user.dto.AgentProfileDTO;

public interface AgentProfileService {

    AppUserAgentProfile getProfile(Long userId);

    AppUserAgentProfile updateProfile(Long userId, AgentProfileDTO profile);
}
