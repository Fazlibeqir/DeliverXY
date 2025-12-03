package com.deliverXY.backend.NewCode.notifications.service;

import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.notifications.domain.UserDeviceToken;
import com.deliverXY.backend.NewCode.notifications.repository.UserDeviceTokenRepository;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDeviceTokenServiceImpl implements UserDeviceTokenService {

    private final UserDeviceTokenRepository tokenRepo;
    private final AppUserService userService;

    @Override
    @Transactional
    public void registerOrUpdateToken(Long userId, String token, String platform) {
        var user = userService.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        // Check for existing token for this user/platform combination
        var existingTokenOpt = tokenRepo.findByUserIdAndDeviceToken(userId, token)
                .stream()
                .filter(t -> t.getPlatform().equalsIgnoreCase(platform))
                .findFirst();

        UserDeviceToken entity = existingTokenOpt.orElseGet(UserDeviceToken::new);

        // Only save if it's new or if the active status needs to be reset
        if (entity.getId() == null) {
            entity.setUser(user);
            entity.setDeviceToken(token);
            entity.setPlatform(platform);
        }

        // Ensure it's marked active
        entity.setActive(true);
        tokenRepo.save(entity);
    }

    @Override
    @Transactional
    public void deregisterToken(Long userId, String token) {
        // Find all tokens matching this user and token string
        var tokens = tokenRepo.findByUserIdAndDeviceToken(userId, token);

        // Mark them as inactive instead of deleting (better for historical tracking)
        tokens.forEach(t -> {
            t.setActive(false);
            tokenRepo.save(t);
        });
    }
}
