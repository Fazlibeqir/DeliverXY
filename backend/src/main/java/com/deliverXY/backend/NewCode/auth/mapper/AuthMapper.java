package com.deliverXY.backend.NewCode.auth.mapper;

import com.deliverXY.backend.NewCode.auth.dto.AuthResponseDTO;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.dto.UserResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public UserResponseDTO toBasicUser(AppUser user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getIsActive(),
                user.getIsVerified(),
                null,
                null,
                null,
                null
        );
    }

    public AuthResponseDTO toAuthResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserResponseDTO userDto
    ) {
        return new AuthResponseDTO(accessToken, refreshToken, expiresIn, userDto);
    }
}

