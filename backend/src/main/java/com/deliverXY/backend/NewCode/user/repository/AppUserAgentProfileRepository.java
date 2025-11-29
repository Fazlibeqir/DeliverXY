package com.deliverXY.backend.NewCode.user.repository;

import com.deliverXY.backend.NewCode.user.domain.AppUserAgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserAgentProfileRepository extends JpaRepository<AppUserAgentProfile, Long> {
}
