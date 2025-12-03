package com.deliverXY.backend.NewCode.user.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsDTO {
    private Double rating;
    private Integer totalDeliveries;
    private BigDecimal totalEarnings;
}
