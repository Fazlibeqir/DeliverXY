package com.deliverXY.backend.NewCode.payments.dto;
import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultDTO {

    private Long paymentId;
    private Long deliveryId;

    private PaymentStatus status;
    private PaymentProvider provider;

    private String providerReference;    // PaymentIntent ID / CPAY Tx ID / WALLET
    private String providerChargeId;     // Stripe charge ID (optional)
    private String message;              // optional system message

    public PaymentResultDTO(String cashOnDelivery, Long id, PaymentProvider provider) {
        this.paymentId = id;
        this.provider = provider;
    }
}
