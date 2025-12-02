package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CashProvider {

    private final PaymentRepository paymentRepo;
    private final DeliveryRepository deliveryRepo;

    public PaymentResultDTO initPayment(Long deliveryId) {

        Delivery d = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        Payment p = new Payment();
        p.setDelivery(d);
        p.setProvider(PaymentProvider.CASH);
        p.setStatus(PaymentStatus.PENDING);
        paymentRepo.save(p);

        return new PaymentResultDTO("CASH_ON_DELIVERY", p.getId(), p.getProvider());
    }

    public PaymentResultDTO confirm(String reference) {
        Payment payment = paymentRepo.findByProviderReference(reference)
                .orElseThrow();

        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepo.save(payment);

        return new PaymentResultDTO(null, payment.getId(), payment.getProvider());
    }
}

