package com.deliverXY.backend.NewCode.auth.validator;

import com.deliverXY.backend.NewCode.exceptions.ConflictException;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthValidationService {

    private final AppUserService userService;

    public void validateRegistration(String username, String email) throws ConflictException {

        if (userService.findByUsername(username).isPresent()) {
            throw new ConflictException("Username already exists");
        }

        if (userService.findByEmail(email).isPresent()) {
            throw new ConflictException("Email already exists");
        }
    }
}

