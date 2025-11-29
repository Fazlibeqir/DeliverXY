package com.deliverXY.backend.NewCode.deliveries.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_rating")
@Data
@NoArgsConstructor
public class DeliveryRating {

    @Id
    private Long deliveryId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private Integer clientRating;
    private String clientReview;

    private Integer agentRating;
    private String agentReview;

    private Boolean reviewedByClient = false;
    private Boolean reviewedByAgent = false;
}
