package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.stripe.exception.StripeException;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    PaymentResultDTO initializePayment(Long deliveryId, PaymentProvider provider, Long userId)
            throws StripeException;

    PaymentResultDTO confirmPayment(String providerReference);

    void refund(Long paymentId, BigDecimal amount, String reason);

    PaymentResultDTO payWithWallet(Long deliveryId, Long userId);

    List<Payment> getPaymentsByUser(Long userId);

    List<Payment> getPaymentsByDelivery(Long deliveryId);
}
