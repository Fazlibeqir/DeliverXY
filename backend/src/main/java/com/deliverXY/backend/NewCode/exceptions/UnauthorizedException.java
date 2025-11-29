package com.deliverXY.backend.NewCode.exceptions;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }
}
