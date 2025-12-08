package com.deliverXY.backend.NewCode.payments.dto;

import com.deliverXY.backend.NewCode.common.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromoCodeDTO {
    @NotBlank(message = "Promo code is required")
    private String code;
    @NotBlank(message = "Description is required")
    private String description;
    @NotNull(message = "Discount type is required")
    private DiscountType discountType;
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be positive")
    private BigDecimal discountValue;
    @DecimalMin(value = "0.00", message = "Max discount cannot be negative")
    private BigDecimal maxDiscountAmount;
    @DecimalMin(value = "0.00", message = "Min order amount cannot be negative")
    private BigDecimal minOrderAmount;
    private Integer usageLimit;
    private Integer usagePerUser;
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @NotNull(message = "Is Active status is required")
    private Boolean isActive;
    private Boolean isFirstOrderOnly;
    private Boolean applicableForNewUsersOnly;
}
