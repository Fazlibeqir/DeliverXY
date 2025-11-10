package com.deliverXY.backend.service;

import com.deliverXY.backend.models.Payment;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.AppUser;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    Payment createPaymentIntent(Delivery delivery, AppUser payer);
    Payment processPayment(String paymentIntentId);
    Payment refundPayment(Long paymentId, BigDecimal amount, String reason);
    List<Payment> getPaymentsByUser(Long userId);
    List<Payment> getPaymentsByDelivery(Long deliveryId);
    Payment getPaymentById(Long paymentId);
    void processAgentPayout(AppUser agent, BigDecimal amount);
    void topUpWallet(AppUser user, BigDecimal amount);
    void withdrawFromWallet(AppUser user, BigDecimal amount);
} 