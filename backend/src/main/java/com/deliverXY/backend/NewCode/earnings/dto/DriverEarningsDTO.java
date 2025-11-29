package com.deliverXY.backend.NewCode.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverEarningsDTO {
    private Long deliveryId;
    private Double earnings;
    private Double tip;
    private String createdAt;
}

