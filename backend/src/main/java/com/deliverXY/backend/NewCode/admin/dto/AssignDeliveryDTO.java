package com.deliverXY.backend.NewCode.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDeliveryDTO {
    @NotNull
    private Long agentId;
}
