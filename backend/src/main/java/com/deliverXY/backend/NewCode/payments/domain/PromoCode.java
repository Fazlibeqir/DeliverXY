package com.deliverXY.backend.NewCode.payments.domain;

import com.deliverXY.backend.NewCode.common.enums.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String currency = "MKD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal discountValue; // Percentage or fixed amount in MKD

    private BigDecimal maxDiscountAmount; // Max discount for percentage type

    private BigDecimal minOrderAmount; // Minimum order amount to use promo

    @Column(name = "usage_limit")
    private Integer usageLimit; // Total usage limit (null = unlimited)

    @Column(name = "usage_per_user")
    private Integer usagePerUser = 1; // How many times one user can use it

    @Column(name = "current_usage")
    private Integer currentUsage = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_first_order_only")
    private Boolean isFirstOrderOnly = false;

    @Column(name = "applicable_for_new_users_only")
    private Boolean applicableForNewUsersOnly = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy; // Admin who created it

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }


}