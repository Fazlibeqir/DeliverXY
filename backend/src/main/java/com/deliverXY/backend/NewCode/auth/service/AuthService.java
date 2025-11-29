package com.deliverXY.backend.NewCode.auth.service;

import com.deliverXY.backend.NewCode.auth.dto.AuthResponseDTO;
import com.deliverXY.backend.NewCode.auth.dto.LoginRequest;
import com.deliverXY.backend.NewCode.auth.dto.RefreshTokenRequest;
import com.deliverXY.backend.NewCode.auth.dto.RegisterRequest;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.dto.UserResponseDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequest registrationRequest);

    AuthResponseDTO login(LoginRequest loginRequest);

    AuthResponseDTO refresh(RefreshTokenRequest refreshTokenRequest);

    UserResponseDTO getCurrentUser(UserPrincipal principal);
}