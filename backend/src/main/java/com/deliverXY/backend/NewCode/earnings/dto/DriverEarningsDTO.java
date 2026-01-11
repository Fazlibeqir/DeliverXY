package com.deliverXY.backend.NewCode.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DriverEarningsDTO {
    private Long deliveryId;
    private BigDecimal earnings;
    private BigDecimal tip;
    private String createdAt;
}

