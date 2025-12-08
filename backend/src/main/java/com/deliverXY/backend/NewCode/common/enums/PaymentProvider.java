package com.deliverXY.backend.NewCode.common.enums;

public enum PaymentProvider {
    STRIPE,     // For testing stage
    CPAY,       // Future MKD card payments
    PAYPAL_API, // If you integrate PayPal later
    WALLET,     // Internal balance
    CASH ,       // No online processing
    MOCK,
    ;

    public PaymentMethod getDefaultMethod() {
        return switch (this) {
            case STRIPE, PAYPAL_API -> PaymentMethod.CREDIT_CARD;
            case CPAY -> PaymentMethod.BANK_TRANSFER;
            case WALLET -> PaymentMethod.WALLET;
            case CASH -> PaymentMethod.CASH;
            case MOCK -> PaymentMethod.CREDIT_CARD;
        };
    }
}
