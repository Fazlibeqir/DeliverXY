package com.deliverXY.backend.NewCode.notifications.repository;

import com.deliverXY.backend.NewCode.notifications.domain.UserDeviceToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    Page<UserDeviceToken> findByDeviceToken(String deviceToken, Pageable pageable);

    Page<UserDeviceToken> findByUserIdAndDeviceToken(Long userId, String token, Pageable pageable);

    Page<UserDeviceToken> findByUserIdAndActiveTrue(Long userId, Pageable pageable);
}
