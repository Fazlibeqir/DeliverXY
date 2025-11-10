package com.deliverXY.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    CLIENT("Client"),
    AGENT("Agent"),
    ADMIN("Admin");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.name().equalsIgnoreCase(value) ||
                    role.description.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }
}
