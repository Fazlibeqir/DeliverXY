
package com.deliverXY.backend.enums;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    PENDING("Pending request"),
    REQUESTED("Delivery requested"),
    ASSIGNED("Driver assigned (legacy alias)"),
    DRIVER_ASSIGNED("Driver assigned"),
    DRIVER_ARRIVED_PICKUP("Driver arrived at pickup"),
    PICKED_UP("Package picked up"),
    IN_TRANSIT("In transit"),
    DRIVER_ARRIVED_DROPOFF("Driver arrived at dropoff"),
    DELIVERED("Delivered successfully"),
    CANCELLED("Cancelled");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    /**
     * Get the enum value itself (useful for consistency)
     * In most cases, you can just use the enum directly instead of calling this
     */
    public DeliveryStatus getDeliveryStatus() {
        return this; // Returns the enum instance itself
    }

    /**
     * Get DeliveryStatus from string (case-insensitive)
     */
    public static DeliveryStatus fromString(String status) {
        if (status == null) {
            return null;
        }

        try {
            return DeliveryStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid delivery status: " + status);
        }
    }

    /**
     * Check if status represents a completed delivery
     */
    public boolean isCompleted() {
        return this == DELIVERED;
    }

    /**
     * Check if status represents a cancelled delivery
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * Check if status is terminal (no further state changes)
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    /**
     * Check if delivery is in progress
     */
    public boolean isInProgress() {
        return this == DRIVER_ASSIGNED ||
                this == DRIVER_ARRIVED_PICKUP ||
                this == PICKED_UP ||
                this == IN_TRANSIT ||
                this == DRIVER_ARRIVED_DROPOFF;
    }
}