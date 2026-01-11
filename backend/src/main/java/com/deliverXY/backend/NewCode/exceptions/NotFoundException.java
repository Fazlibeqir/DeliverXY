package com.deliverXY.backend.NewCode.exceptions;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}
