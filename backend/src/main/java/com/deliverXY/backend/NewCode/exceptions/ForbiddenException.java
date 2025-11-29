package com.deliverXY.backend.NewCode.exceptions;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN");
    }
}
