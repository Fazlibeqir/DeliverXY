package com.deliverXY.backend.NewCode.common.enums;

public enum PaymentProvider {
    STRIPE,     // For testing stage
    CPAY,       // Future MKD card payments
    PAYPAL_API, // If you integrate PayPal later
    WALLET,     // Internal balance
    CASH        // No online processing
}
