package com.deliverXY.backend.NewCode.payments.repository;


import com.deliverXY.backend.NewCode.payments.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByProviderReference(String reference);
    Optional<Payment> findByProviderSessionId(String sessionId);


    List<Payment> findByPayerId(Long payerId);

    Optional<Payment> findByDeliveryId(Long deliveryId);

    List<Payment> findByDeliveryIdIn(Collection<Long> deliveryIds);

    @Query("""
            SELECT COALESCE(SUM(p.platformFee), 0) FROM Payment p
            WHERE p.escrowReleased = true AND COALESCE(p.platformFee, 0) > 0
            """)
    BigDecimal sumPositivePlatformFees();

    @Query("""
            SELECT COALESCE(SUM(
                p.amount - COALESCE(p.driverAmount, 0) - COALESCE(p.tip, 0)
            ), 0) FROM Payment p
            WHERE p.escrowReleased = true
              AND COALESCE(p.platformFee, 0) = 0
              AND p.amount > COALESCE(p.driverAmount, 0) + COALESCE(p.tip, 0)
            """)
    BigDecimal sumDerivedPlatformFees();
}
