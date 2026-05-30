package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.exceptions.UnauthorizedException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromoCodeUserService {

    private final AppUserService appUserService;

    @Transactional(readOnly = true)
    public AppUser requireByEmail(String email) {
        return appUserService.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
