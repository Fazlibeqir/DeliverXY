package com.deliverXY.backend.NewCode.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminEarningsDTO {
    private BigDecimal totalPlatformRevenue;
    private BigDecimal totalDriverEarnings;
    private Long totalDelivered;
}
