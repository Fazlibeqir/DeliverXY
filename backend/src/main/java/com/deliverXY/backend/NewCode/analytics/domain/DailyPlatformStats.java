package com.deliverXY.backend.NewCode.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "daily_platform_stats")
@Data
public class DailyPlatformStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate statDate;

    private Integer totalDeliveries;
    private Integer completedDeliveries;
    private Integer cancelledDeliveries;

    private Double totalRevenue;
    private Double totalDistanceKm;
}
