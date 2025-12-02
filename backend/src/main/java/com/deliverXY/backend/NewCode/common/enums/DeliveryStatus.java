package com.deliverXY.backend.NewCode.common.enums;

import lombok.Getter;

@Getter
public enum DeliveryStatus {

    PENDING("Pending request"),
    REQUESTED("Delivery requested"),

    ASSIGNED("Driver assigned (legacy alias)"),


    PICKED_UP("Package picked up"),
    IN_TRANSIT("In transit"),

    // Terminal states
    DELIVERED("Delivered successfully"),
    CANCELLED("Cancelled");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    /**
     * Parse string → DeliveryStatus (case-insensitive, handles aliases).
     */
    public static DeliveryStatus fromString(String status) {
        if (status == null) return null;

        String s = status.trim().toUpperCase();

        // Handle legacy aliases
        if (s.equals("ASSIGNED"))
            return ASSIGNED;

        if (s.equals("PENDING"))
            return REQUESTED; // Normalize early state

        try {
            return DeliveryStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid delivery status: " + status);
        }
    }

    public boolean isCompleted() {
        return this == DELIVERED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isTerminal() {
        return isCompleted() || isCancelled();
    }

    public boolean isInProgress() {
        return switch (this) {
            case ASSIGNED , PICKED_UP, IN_TRANSIT -> true;
            default -> false;
        };
    }
}
