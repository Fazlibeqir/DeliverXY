package com.deliverXY.backend.NewCode.deliveries.repository;

import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DeliveryPaymentRepository extends JpaRepository<DeliveryPayment, Long> {

    Optional<DeliveryPayment> findByDeliveryId(Long id);
}