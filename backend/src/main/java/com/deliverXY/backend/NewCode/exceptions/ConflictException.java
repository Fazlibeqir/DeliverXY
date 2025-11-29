package com.deliverXY.backend.NewCode.exceptions;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, "CONFLICT");
    }
}