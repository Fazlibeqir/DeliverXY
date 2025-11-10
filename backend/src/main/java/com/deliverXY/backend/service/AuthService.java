package com.deliverXY.backend.service;

import com.deliverXY.backend.models.dto.ReqRes;

public interface AuthService {
    ReqRes signUp(ReqRes registrationRequest);
    ReqRes signIn(ReqRes loginRequest);
    ReqRes refreshToken(ReqRes refreshTokenRequest);
}