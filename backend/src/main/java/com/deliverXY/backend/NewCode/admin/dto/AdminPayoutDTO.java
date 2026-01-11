package com.deliverXY.backend.NewCode.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminPayoutDTO {
    private Long id;
    private Long driverId;
    private String driverName;
    private String driverEmail;
    private BigDecimal amountPaid;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String status;
    private LocalDateTime paidAt;
    private String transactionRef;
    private String processedBy;
}
