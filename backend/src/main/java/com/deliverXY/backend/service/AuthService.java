package com.deliverXY.backend.service;

import com.deliverXY.backend.models.dto.ReqRes;

public interface AuthService {
    ReqRes signUp(ReqRes registrationRequest);
    ReqRes signIn(ReqRes signingRequest);
    ReqRes refreshToken(ReqRes refreshTokenRequest);
}