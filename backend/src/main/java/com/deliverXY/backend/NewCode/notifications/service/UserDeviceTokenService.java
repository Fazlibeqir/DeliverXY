package com.deliverXY.backend.NewCode.notifications.service;

public interface UserDeviceTokenService {
    void registerOrUpdateToken(Long userId, String token, String platform);
    void deregisterToken(Long userId, String token);
}
