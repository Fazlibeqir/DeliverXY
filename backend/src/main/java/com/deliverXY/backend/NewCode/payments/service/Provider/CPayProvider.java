package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import org.springframework.stereotype.Service;

@Service
public class CPayProvider {

    public PaymentResultDTO initPayment(Long deliveryId) {
        throw new UnsupportedOperationException("CPAY INTEGRATION COMING SOON");
    }

    public PaymentResultDTO confirm(String reference) {
        throw new UnsupportedOperationException("CPAY INTEGRATION COMING SOON");
    }
}

