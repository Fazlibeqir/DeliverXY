package com.deliverXY.backend.NewCode.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "app_user_stats")
@Data
@NoArgsConstructor
public class AppUserStats {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;


    private Double rating = 0.0;
    private Integer totalDeliveries = 0;
    private BigDecimal totalEarnings = BigDecimal.ZERO;
}
