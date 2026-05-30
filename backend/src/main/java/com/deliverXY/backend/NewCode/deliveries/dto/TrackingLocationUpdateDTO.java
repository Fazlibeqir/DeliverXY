package com.deliverXY.backend.NewCode.deliveries.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrackingLocationUpdateDTO {

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;
}
