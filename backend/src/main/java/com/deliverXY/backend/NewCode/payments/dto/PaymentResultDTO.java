package com.deliverXY.backend.NewCode.payments.dto;
import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultDTO {

    private Long paymentId;
    private Long deliveryId;

    private PaymentStatus status;
    private PaymentProvider provider;

    private BigDecimal amountPaid;

    private String providerReference;    // PaymentIntent ID / CPAY Tx ID / WALLET
    private String providerSessionId;
    private String providerChargeId;     // Stripe charge ID (optional)
    private String message;              // optional system message

    public PaymentResultDTO(Long paymentId, Long deliveryId, PaymentProvider provider, PaymentStatus status) {
        this.paymentId = paymentId;
        this.deliveryId = deliveryId;
        this.provider = provider;
        this.status = status;
        this.message = "Cash on Delivery payment recorded successfully.";
    }
}
