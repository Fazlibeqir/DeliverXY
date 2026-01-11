package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;

import java.math.BigDecimal;

public interface PaymentGatewayProvider {


    PaymentProvider getProviderType();
    /**
     * Initializes a transaction with the external provider.
     * @param payment The payment entity representing the transaction.
     * @return A DTO containing status, reference, and optionally a redirect URL.
     */
    PaymentResultDTO initiateTransaction(Payment payment);

    /**
     * Confirms the status of a transaction using the provider's reference.
     * @param providerReference The unique transaction reference from the provider.
     * @return A DTO containing the confirmed payment status.
     */
    PaymentResultDTO confirmTransaction(String providerReference);

    /**
     * Refunds a specific amount for a completed payment.
     * @param payment The original payment entity.
     * @param amount The amount to refund.
     * @param reason The reason for the refund.
     */
    void refundTransaction(Payment payment, BigDecimal amount, String reason);
}
