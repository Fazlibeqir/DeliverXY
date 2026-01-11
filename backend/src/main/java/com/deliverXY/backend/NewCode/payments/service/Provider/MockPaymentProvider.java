package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.service.PaymentGatewayProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock implementation of the payment gateway for local development and testing.
 * This class should ONLY be active during testing or development profiles.
 */
@Service
@Profile({"dev", "test", "mock"})
public class MockPaymentProvider implements PaymentGatewayProvider {

    // Helper to generate a unique, mock reference
    private String generateMockReference() {
        return "MOCK-TX-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public PaymentProvider getProviderType() {
        return PaymentProvider.MOCK;
    }

    @Override
    public PaymentResultDTO initiateTransaction(Payment payment) {
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Mock Provider: Payment amount must be positive.");
        }

        // Simulates a successful immediate payment (common for test environments)
        String mockRef = generateMockReference();

        return PaymentResultDTO.builder()
                .paymentId(payment.getId())
                .deliveryId(payment.getDelivery().getId())
                .status(PaymentStatus.COMPLETED)
                .provider(PaymentProvider.MOCK)
                .amountPaid(payment.getAmount())
                .providerReference(mockRef)
                .message("Mock payment successful (instant).")
                .build();
    }

    @Override
    public PaymentResultDTO confirmTransaction(String providerReference) {
        // Assume any valid-looking reference is confirmed successfully
        if (providerReference == null || !providerReference.startsWith("MOCK-TX-")) {
            return PaymentResultDTO.builder()
                    .status(PaymentStatus.FAILED)
                    .provider(PaymentProvider.MOCK)
                    .providerReference(providerReference)
                    .message("Mock Provider: Invalid reference format.")
                    .build();
        }

        // Simulates a successful confirmation for an existing transaction
        return PaymentResultDTO.builder()
                .status(PaymentStatus.COMPLETED)
                .provider(PaymentProvider.MOCK)
                .providerReference(providerReference)
                .message("Mock Provider: Transaction confirmed successfully.")
                .build();
    }

    @Override
    public void refundTransaction(Payment payment, BigDecimal amount, String reason) {
        // Simulates an immediate, successful refund
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException("Mock Provider: Invalid refund amount.");
        }

        // Simulate refund (no-op)
        System.out.printf("MOCK Refund: PaymentId=%d Amount=%s Reason=%s%n",
                payment.getId(), amount.toPlainString(), reason);
    }
}
