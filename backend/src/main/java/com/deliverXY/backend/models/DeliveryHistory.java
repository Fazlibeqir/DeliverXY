package com.deliverXY.backend.models;

import com.deliverXY.backend.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private String notes;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    private Double latitude;

    private Double longitude;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}