package com.deliverXY.backend.NewCode.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectKycRequest {

    @NotBlank
    @Size(max = 500)
    private String reason;
}
