package com.deliverXY.backend.NewCode.auth.factory;

import com.deliverXY.backend.NewCode.auth.dto.RegisterRequest;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final PasswordEncoder encoder;

    public AppUser create(RegisterRequest req) {
        AppUser user = new AppUser();

        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPassword(encoder.encode(req.getPassword()));

        user.setRole(req.getRole() != null ? req.getRole() : UserRole.CLIENT);
        user.setIsActive(true);
        user.setIsVerified(false);

        return user;
    }
}

