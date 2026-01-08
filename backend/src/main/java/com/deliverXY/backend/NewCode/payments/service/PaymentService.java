package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    PaymentResultDTO initializePayment(Long deliveryId, BigDecimal amount, PaymentProvider provider, Long userId);

    PaymentResultDTO confirmPayment(String providerReference);

    void refund(Long paymentId, BigDecimal amount, String reason);


    List<Payment> getPaymentsByUser(Long userId);

}
