package com.deliverXY.backend.NewCode.deliveries.repository;

import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {
    Optional<DeliveryTracking> findByDeliveryId(Long deliveryId);
}
