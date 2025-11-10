package com.deliverXY.backend.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    BANK_TRANSFER("Bank Transfer"),
    WALLET("Digital Wallet"),
    CASH("Cash"),
    PAYPAL("PayPal"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

}