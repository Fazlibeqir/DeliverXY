package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.service.PaymentGatewayProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProvider implements PaymentGatewayProvider {


    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init(){
        log.info("Stripe key loaded: {}", (secretKey != null && !secretKey.isEmpty()));
        if (secretKey != null && !secretKey.isEmpty()) {
            Stripe.apiKey = secretKey;
        } else {
            log.warn("Stripe secret key not configured. Stripe payments will not work.");
        }
    }

    @Override
    public PaymentProvider getProviderType() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public PaymentResultDTO initiateTransaction(Payment payment) {


        try {
            long amountInMinor = payment.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue(); // mkd -> "minor units" if your account supports it

            PaymentIntent intent = PaymentIntent.create(
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountInMinor)
                            .setCurrency("mkd")
                            .putMetadata("payment_id", payment.getId().toString())
                            .putMetadata("delivery_id", payment.getDelivery().getId().toString())
                            .build()
            );

            return PaymentResultDTO.builder()
                    .paymentId(payment.getId())
                    .deliveryId(payment.getDelivery().getId())
                    .provider(PaymentProvider.STRIPE)
                    .status(PaymentStatus.PENDING)
                    .providerReference(intent.getId())          // PaymentIntent ID
                    .providerSessionId(intent.getClientSecret())// clientSecret for client SDK
                    .amountPaid(payment.getAmount())
                    .message("Stripe PaymentIntent created.")
                    .build();

        } catch (StripeException e) {
            return PaymentResultDTO.builder()
                    .paymentId(payment.getId())
                    .deliveryId(payment.getDelivery().getId())
                    .provider(PaymentProvider.STRIPE)
                    .status(PaymentStatus.FAILED)
                    .message("Stripe initiate failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentResultDTO confirmTransaction(String providerReference) {

        try {
            PaymentIntent intent = PaymentIntent.retrieve(providerReference);

            if ("succeeded".equals(intent.getStatus())) {
                return PaymentResultDTO.builder()
                        .provider(PaymentProvider.STRIPE)
                        .status(PaymentStatus.COMPLETED)
                        .providerReference(providerReference)
                        .message("Stripe payment succeeded.")
                        .build();
            }

            if ("requires_payment_method".equals(intent.getStatus())
                    || "canceled".equals(intent.getStatus())) {
                return PaymentResultDTO.builder()
                        .provider(PaymentProvider.STRIPE)
                        .status(PaymentStatus.FAILED)
                        .providerReference(providerReference)
                        .message("Stripe payment failed: " + intent.getStatus())
                        .build();
            }

            return PaymentResultDTO.builder()
                    .provider(PaymentProvider.STRIPE)
                    .status(PaymentStatus.PROCESSING)
                    .providerReference(providerReference)
                    .message("Stripe payment not completed yet: " + intent.getStatus())
                    .build();

        } catch (StripeException e) {
            return PaymentResultDTO.builder()
                    .provider(PaymentProvider.STRIPE)
                    .status(PaymentStatus.FAILED)
                    .providerReference(providerReference)
                    .message("Stripe confirmation failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Refunds a Stripe payment transaction.
     * 
     * @param payment The payment to refund
     * @param amount The amount to refund (must be <= original payment amount)
     * @param reason The reason for the refund
     * @throws RuntimeException if the refund fails
     */
    @Override
    public void refundTransaction(Payment payment, BigDecimal amount, String reason) {
        if (payment.getProviderReference() == null || payment.getProviderReference().isEmpty()) {
            throw new IllegalArgumentException("Cannot refund payment: missing provider reference");
        }

        if (amount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed original payment amount");
        }

        try {
            // Convert amount to minor units (cents for most currencies)
            long amountInMinor = amount.multiply(BigDecimal.valueOf(100)).longValue();

            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getProviderReference())
                    .setAmount(amountInMinor)
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .putMetadata("refund_reason", reason != null ? reason : "Refund requested")
                    .putMetadata("payment_id", payment.getId().toString())
                    .build();

            Refund refund = Refund.create(params);
            
            log.info("Stripe refund created successfully. Refund ID: {}, Status: {}, Amount: {}", 
                     refund.getId(), refund.getStatus(), amount);

            if ("failed".equals(refund.getStatus())) {
                throw new RuntimeException("Stripe refund failed: " + refund.getFailureReason());
            }

        } catch (StripeException e) {
            log.error("Failed to refund Stripe payment {}: {}", payment.getId(), e.getMessage());
            throw new RuntimeException("Stripe refund failed: " + e.getMessage(), e);
        }
    }
}

