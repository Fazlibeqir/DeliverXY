package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.service.PaymentGatewayProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripePaymentProvider implements PaymentGatewayProvider {


    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init(){
        System.out.println("Stripe key loaded: " + (secretKey != null));
        Stripe.apiKey = secretKey;
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
    @Override
    public void refundTransaction(Payment payment, BigDecimal amount, String reason) {
        throw new UnsupportedOperationException("Stripe refund not implemented yet.");
    }
}

