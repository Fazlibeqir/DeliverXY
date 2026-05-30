package com.deliverXY.backend.NewCode.common.util;

import com.deliverXY.backend.NewCode.exceptions.BaseException;

public final class ExceptionMessageSanitizer {

    private ExceptionMessageSanitizer() {
    }

    public static String clientMessage(BaseException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "Request failed";
        }
        if (message.contains("..") || message.contains("\0") || message.contains("/")) {
            return "Request failed";
        }
        return message;
    }

    public static String validationFailureMessage() {
        return "Validation failed";
    }

    public static String operationFailureMessage() {
        return "Operation failed";
    }
}
