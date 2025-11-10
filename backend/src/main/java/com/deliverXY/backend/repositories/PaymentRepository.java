package com.deliverXY.backend.repositories;

import com.deliverXY.backend.models.Payment;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByPayer(AppUser payer);
    List<Payment> findByRecipient(AppUser recipient);
    List<Payment> findByDelivery(Delivery delivery);
    List<Payment> findByPaymentType(Payment.PaymentType paymentType);
    List<Payment> findByStatus(Payment.PaymentStatus status);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    Optional<Payment> findByStripeChargeId(String stripeChargeId);
} 