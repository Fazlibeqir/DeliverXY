package com.deliverXY.backend.NewCode.notifications.repository;

import com.deliverXY.backend.NewCode.notifications.domain.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    List<UserDeviceToken> findByUserId(Long userId);

    List<UserDeviceToken> findByDeviceToken(String deviceToken);

    void deleteByDeviceToken(String token);
}
