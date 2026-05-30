package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.notifications.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final UserDeviceTokenRepository tokenRepo;

    @Transactional
    public void deactivateByDeviceToken(String deviceToken) {
        tokenRepo.findByDeviceToken(deviceToken, PageRequest.of(0, 20)).getContent().forEach(t -> {
            t.setActive(false);
            tokenRepo.save(t);
        });
    }
}
