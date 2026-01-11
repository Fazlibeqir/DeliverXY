package com.deliverXY.backend.NewCode.deliveries.repository;

import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, Long> {

    List<DeliveryHistory> findByDeliveryOrderByChangedAtAsc(Delivery delivery);

    List<DeliveryHistory> findByDelivery_IdOrderByChangedAtAsc(Long deliveryId);
}