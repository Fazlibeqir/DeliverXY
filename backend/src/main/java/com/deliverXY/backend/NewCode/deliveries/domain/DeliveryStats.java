package com.deliverXY.backend.NewCode.deliveries.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(
        name = "delivery_stats",
        indexes = {
                @Index(name = "idx_stats_date", columnList = "stat_date")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false, unique = true)
    private LocalDate statDate;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column(name = "completed_deliveries")
    private Integer completedDeliveries = 0;

    @Column(name = "cancelled_deliveries")
    private Integer cancelledDeliveries = 0;

    @Column(name = "total_revenue")
    private Double totalRevenue = 0.0;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm = 0.0;

    @Column(name = "average_delivery_time_minutes")
    private Integer averageDeliveryTimeMinutes = 0;

    @Column(name = "active_drivers")
    private Integer activeDrivers = 0;

    @Column(name = "active_customers")
    private Integer activeCustomers = 0;

    @Column(name = "peak_hour_deliveries")
    private Integer peakHourDeliveries = 0;
}