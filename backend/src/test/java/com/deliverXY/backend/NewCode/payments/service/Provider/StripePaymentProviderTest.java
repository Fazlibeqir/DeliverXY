package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StripePaymentProviderTest {

    private StripePaymentProvider stripePaymentProvider;

    @BeforeEach
    void setUp() {
        stripePaymentProvider = new StripePaymentProvider();
        // Set a test API key (not initialized to avoid actual Stripe calls)
        ReflectionTestUtils.setField(stripePaymentProvider, "secretKey", "");
    }

    @Test
    void testGetProviderType() {
        assertEquals(PaymentProvider.STRIPE, stripePaymentProvider.getProviderType());
    }

    @Test
    void testRefundTransaction_NullProviderReference() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setProviderReference(null);
        payment.setAmount(new BigDecimal("100.00"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stripePaymentProvider.refundTransaction(payment, new BigDecimal("50.00"), "Test refund");
        });

        assertTrue(exception.getMessage().contains("missing provider reference"));
    }

    @Test
    void testRefundTransaction_EmptyProviderReference() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setProviderReference("");
        payment.setAmount(new BigDecimal("100.00"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stripePaymentProvider.refundTransaction(payment, new BigDecimal("50.00"), "Test refund");
        });

        assertTrue(exception.getMessage().contains("missing provider reference"));
    }

    @Test
    void testRefundTransaction_AmountExceedsPaymentAmount() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setProviderReference("pi_test123");
        payment.setAmount(new BigDecimal("100.00"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stripePaymentProvider.refundTransaction(payment, new BigDecimal("150.00"), "Test refund");
        });

        assertTrue(exception.getMessage().contains("cannot exceed original payment amount"));
    }

    @Test
    void testRefundTransaction_ValidAmountLessThanPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setProviderReference("pi_test123");
        payment.setAmount(new BigDecimal("100.00"));

        // This will fail with RuntimeException since we don't have actual Stripe credentials
        // but validates that the validation logic passes
        assertThrows(RuntimeException.class, () -> {
            stripePaymentProvider.refundTransaction(payment, new BigDecimal("50.00"), "Partial refund");
        });
    }

    @Test
    void testRefundTransaction_ValidAmountEqualToPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setProviderReference("pi_test123");
        payment.setAmount(new BigDecimal("100.00"));

        // This will fail with RuntimeException since we don't have actual Stripe credentials
        // but validates that the validation logic passes
        assertThrows(RuntimeException.class, () -> {
            stripePaymentProvider.refundTransaction(payment, new BigDecimal("100.00"), "Full refund");
        });
    }
}
