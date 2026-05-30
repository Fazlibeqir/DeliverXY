package com.deliverXY.backend.NewCode.admin.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProcessPayoutRequest {

    @Size(max = 128)
    private String transactionRef;
}
