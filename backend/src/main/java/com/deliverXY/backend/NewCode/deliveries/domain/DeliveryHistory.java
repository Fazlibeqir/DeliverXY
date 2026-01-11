package com.deliverXY.backend.NewCode.deliveries.domain;

import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_history")
@Data
@NoArgsConstructor
public class DeliveryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private LocalDateTime changedAt = LocalDateTime.now();

    private String changedBy; // CLIENT / AGENT / SYSTEM / ADMIN
    private String note;
}
