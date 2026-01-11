package com.deliverXY.backend.NewCode.user.repository;

import com.deliverXY.backend.NewCode.user.domain.AppUserStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserStatsRepository extends JpaRepository<AppUserStats, Long> {
}
