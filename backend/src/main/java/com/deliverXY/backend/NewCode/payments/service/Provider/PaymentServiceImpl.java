package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.deliverXY.backend.NewCode.payments.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final StripeProvider stripe;
    private final CPayProvider cpay;
    private final WalletProvider wallet;
    private final CashProvider cash;
    private final PaymentRepository paymentRepo;

    @Override
    public PaymentResultDTO initializePayment(Long deliveryId, PaymentProvider provider, Long userId)
            throws StripeException {

        return switch (provider) {
            case STRIPE -> stripe.initPayment(deliveryId);
            case CPAY -> cpay.initPayment(deliveryId);
            case WALLET -> wallet.initPayment(deliveryId);
            case CASH -> cash.initPayment(deliveryId);
            case PAYPAL_API -> throw new UnsupportedOperationException("PayPal not implemented");
        };
    }

    @Override
    public PaymentResultDTO confirmPayment(String reference) {

        Payment payment = paymentRepo.findByProviderReference(reference)
                .orElseThrow(() -> new RuntimeException("Invalid payment reference"));

        return switch (payment.getProvider()) {
            case STRIPE -> stripe.confirm(reference);
            case CPAY -> cpay.confirm(reference);
            case WALLET -> wallet.confirm(reference);
            case CASH -> cash.confirm(reference);
            case PAYPAL_API -> throw new UnsupportedOperationException("PayPal not implemented");
        };
    }

    @Override
    public void refund(Long paymentId, BigDecimal amount, String reason) {
        // TODO: implement per provider logic here
    }

    @Override
    public PaymentResultDTO payWithWallet(Long deliveryId, Long userId) {
        return wallet.initPayment(deliveryId);
    }

    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepo.findByPayerId(userId);
    }

    @Override
    public List<Payment> getPaymentsByDelivery(Long deliveryId) {
        return paymentRepo.findByDeliveryId(deliveryId);
    }
}
