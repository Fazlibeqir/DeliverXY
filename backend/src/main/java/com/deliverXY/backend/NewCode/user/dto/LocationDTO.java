package com.deliverXY.backend.NewCode.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LocationDTO {
    private Double latitude;
    private Double longitude;
    private LocalDateTime updatedAt;
}
