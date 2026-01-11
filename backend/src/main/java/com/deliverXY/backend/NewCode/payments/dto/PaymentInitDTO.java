package com.deliverXY.backend.NewCode.payments.dto;


import com.deliverXY.backend.NewCode.common.enums.PaymentMethod;
import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitDTO {

    private Long paymentId;
    private Long deliveryId;

    private PaymentMethod method;
    private PaymentProvider provider;

    // Provider-specific fields:
    private String clientSecret;   // Stripe client secret
    private String redirectUrl;    // CPay URL or PayPal URL
    private String providerReference; // PaymentIntent ID or other
}
