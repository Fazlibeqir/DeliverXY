package com.deliverXY.backend.NewCode.user.service;

import com.deliverXY.backend.NewCode.user.domain.AppUserAgentProfile;

public interface AgentProfileService {

    AppUserAgentProfile getProfile(Long userId);

    AppUserAgentProfile updateProfile(Long userId, AppUserAgentProfile profile);
}
