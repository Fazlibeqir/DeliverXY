package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

//@Service
@RequiredArgsConstructor
public class StripeProvider {

//    @Value(staticConstructor = "${stripe.secret-key}")
    private String secretKey;

    private final PaymentRepository paymentRepo;
    private final DeliveryRepository deliveryRepo;

    public PaymentResultDTO initPayment(Long deliveryId) throws StripeException {
        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        Stripe.apiKey = secretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(delivery.getDeliveryPayment().getFinalAmount().multiply(BigDecimal.valueOf(100)).longValue())
                .setCurrency("mkd")   // MKD NOW SUPPORTED
                .putMetadata("delivery_id", delivery.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Payment p = new Payment();
        p.setDelivery(delivery);
        p.setProvider(PaymentProvider.STRIPE);
        p.setProviderReference(intent.getId());
        p.setStatus(PaymentStatus.PENDING);
        paymentRepo.save(p);

        return new PaymentResultDTO(intent.getClientSecret(), p.getId(), p.getProvider());
    }

    public PaymentResultDTO confirm(String reference) {
        Stripe.apiKey = secretKey;

        PaymentIntent intent = null;
        try {
            intent = PaymentIntent.retrieve(reference);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        Payment payment = paymentRepo.findByProviderReference(reference)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (intent.getStatus().equals("succeeded")) {
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepo.save(payment);
        }

        return new PaymentResultDTO(null, payment.getId(), payment.getProvider());
    }
}

