package com.deliverXY.backend.NewCode.payments.repository;


import com.deliverXY.backend.NewCode.payments.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByProviderReference(String reference);
    Optional<Payment> findByProviderSessionId(String sessionId);


    List<Payment> findByPayerId(Long payerId);

    Optional<Payment> findByDeliveryId(Long deliveryId);
}
