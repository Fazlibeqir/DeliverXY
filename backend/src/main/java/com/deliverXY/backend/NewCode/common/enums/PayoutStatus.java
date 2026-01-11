package com.deliverXY.backend.NewCode.common.enums;

import lombok.Getter;

@Getter
public enum PayoutStatus {
    PENDING("Pending processing"),
    ON_HOLD( "On hold same as pending"),
    PROCESSING("Being processed"),
    PAID("Same as Completed"),
    COMPLETED("Completed successfully"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String description;

    PayoutStatus(String description) {
        this.description = description;
    }

}
