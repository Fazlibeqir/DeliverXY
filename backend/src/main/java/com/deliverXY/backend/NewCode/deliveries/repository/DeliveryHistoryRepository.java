package com.deliverXY.backend.NewCode.deliveries.repository;

import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, Long> {

    Page<DeliveryHistory> findByDeliveryOrderByChangedAtAsc(Delivery delivery, Pageable pageable);

    Page<DeliveryHistory> findByDelivery_IdOrderByChangedAtAsc(Long deliveryId, Pageable pageable);

    void deleteByDelivery_Id(Long deliveryId);
}
