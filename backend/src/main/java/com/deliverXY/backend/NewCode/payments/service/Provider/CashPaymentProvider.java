package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.deliverXY.backend.NewCode.payments.service.PaymentGatewayProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashPaymentProvider implements PaymentGatewayProvider {

    @Override
    public PaymentProvider getProviderType() {
        return PaymentProvider.CASH;
    }

    @Override
    public PaymentResultDTO initiateTransaction(Payment payment) {
        // Cash has no external gateway reference, but we still create a reference for tracking.
        String ref = "CASH_" + payment.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);

        return PaymentResultDTO.builder()
                .paymentId(payment.getId())
                .deliveryId(payment.getDelivery().getId())
                .status(PaymentStatus.PENDING)
                .provider(PaymentProvider.CASH)
                .providerReference(ref)
                .message("Cash on delivery selected (awaiting manual confirmation).")
                .build();
    }

    @Override
    public PaymentResultDTO confirmTransaction(String providerReference) {
        return PaymentResultDTO.builder()
                .status(PaymentStatus.PROCESSING)
                .provider(PaymentProvider.CASH)
                .providerReference(providerReference)
                .message("Cash payment awaiting manual confirmation.")
                .build();
    }

    @Override
    public void refundTransaction(Payment payment, BigDecimal amount, String reason) {
        throw new UnsupportedOperationException("Cash payments cannot be refunded automatically");
    }
}

