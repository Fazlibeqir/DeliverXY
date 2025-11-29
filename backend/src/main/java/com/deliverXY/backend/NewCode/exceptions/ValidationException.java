
package com.deliverXY.backend.NewCode.exceptions;

public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(message, "VALIDATION_FAILED");
    }
}