package com.deliverXY.backend.NewCode.deliveries.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_tracking")
@Data
@NoArgsConstructor
public class DeliveryTracking {

    @Id
    private Long deliveryId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private Double currentLatitude;
    private Double currentLongitude;

    private Double estimatedDistance;
    private Integer estimatedDuration;

    private LocalDateTime lastLocationUpdate;
}
