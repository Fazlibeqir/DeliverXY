package com.deliverXY.backend.NewCode.user.dto;

import lombok.Data;

@Data
public class AgentStatusUpdateDTO {
    private Boolean isAvailable;
    private Double currentLatitude;
    private Double currentLongitude;
}

