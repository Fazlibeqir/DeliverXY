package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.service.PaymentGatewayProvider;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletPaymentProvider implements PaymentGatewayProvider {

    private final WalletService walletService;

    @Override
    public PaymentProvider getProviderType() {
        return PaymentProvider.WALLET;
    }

    @Override
    public PaymentResultDTO initiateTransaction(Payment payment) {
        String ref = "ESCROW_HOLD_" + payment.getDelivery().getId() + "_" + UUID.randomUUID().toString().substring(0, 8);

        walletService.withdraw(
                payment.getPayerId(),
                payment.getAmount(),
                ref
        );
        return PaymentResultDTO.builder()
                .paymentId(payment.getId())
                .deliveryId(payment.getDelivery().getId())
                .status(PaymentStatus.COMPLETED)
                .provider(PaymentProvider.WALLET)
                .amountPaid(payment.getAmount())
                .providerReference(ref)
                .message("Wallet payment held in escrow.")
                .build();
    }

    @Override
    public PaymentResultDTO confirmTransaction(String providerReference) {
        return PaymentResultDTO.builder()
                .provider(PaymentProvider.WALLET)
                .status(PaymentStatus.COMPLETED)
                .providerReference(providerReference)
                .message("Wallet payment already settled internally.")
                .build();
    }

    @Override
    public void refundTransaction(Payment payment, BigDecimal amount, String reason) {
        walletService.deposit(payment.getPayerId(), amount, reason);
    }
}
