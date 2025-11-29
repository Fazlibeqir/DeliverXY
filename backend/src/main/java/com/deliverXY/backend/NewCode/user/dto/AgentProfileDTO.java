package com.deliverXY.backend.NewCode.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentProfileDTO {
    private String driversLicenseNumber;
    private LocalDateTime driversLicenseExpiry;
    private String driversLicenseFrontUrl;
    private String driversLicenseBackUrl;

    private Boolean isAvailable;
}
