package com.deliverXY.backend.repositories;

import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.DeliveryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, Long> {

    List<DeliveryHistory> findByDeliveryOrderByChangedAtAsc(Delivery delivery);

    List<DeliveryHistory> findByDeliveryIdOrderByChangedAtAsc(Long deliveryId);
}