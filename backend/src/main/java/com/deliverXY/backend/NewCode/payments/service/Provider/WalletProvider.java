package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletProvider {

    private final WalletService walletService;
    private final DeliveryRepository deliveryRepo;
    private final PaymentRepository paymentRepo;

    public PaymentResultDTO initPayment(Long deliveryId) {

        Delivery d = deliveryRepo.findById(deliveryId).orElseThrow();

        walletService.withdraw(
                d.getClient().getId(),
                d.getDeliveryPayment().getFinalAmount(),
                "DELIVERY_PAYMENT_" + d.getId()
        );

        Payment p = new Payment();
        p.setProvider(PaymentProvider.WALLET);
        p.setStatus(PaymentStatus.COMPLETED);
        p.setDelivery(d);
        paymentRepo.save(p);

        return new PaymentResultDTO(null, p.getId(), p.getProvider());
    }

    public PaymentResultDTO confirm(String reference) {
        // No confirmation needed for wallet
        Payment payment = paymentRepo.findByProviderReference(reference).orElseThrow();
        return new PaymentResultDTO(null, payment.getId(), payment.getProvider());
    }
}
