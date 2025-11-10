package com.deliverXY.backend.models.dto;

import com.deliverXY.backend.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeDTO {
    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private Double discountValue;
    private Double maxDiscountAmount;
    private Double minOrderAmount;
    private Integer usageLimit;
    private Integer usagePerUser;
    private Integer currentUsage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isFirstOrderOnly;
    private Boolean applicableForNewUsersOnly;
}