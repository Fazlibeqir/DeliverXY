package com.deliverXY.backend.NewCode.exceptions;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST");
    }
}
