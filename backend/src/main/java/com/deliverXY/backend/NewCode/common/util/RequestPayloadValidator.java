package com.deliverXY.backend.NewCode.common.util;

import com.deliverXY.backend.NewCode.exceptions.BadRequestException;

public final class RequestPayloadValidator {

    private RequestPayloadValidator() {
    }

    public static <T> T requireBody(T body) {
        if (body == null) {
            throw new BadRequestException("Request body is required");
        }
        return body;
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.contains("..") || trimmed.contains("\0")) {
            throw new BadRequestException("Invalid " + fieldName);
        }
        return trimmed;
    }
}
