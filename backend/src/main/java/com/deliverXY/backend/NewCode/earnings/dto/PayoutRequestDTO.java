package com.deliverXY.backend.NewCode.earnings.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PayoutRequestDTO {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String paymentMethod;
    private String destination; // bank account, wallet id, etc
}

