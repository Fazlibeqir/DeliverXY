package com.deliverXY.backend.NewCode.exceptions;

public class PaymentException extends BaseException {
    public PaymentException(String message) {
        super(message, "PAYMENT_ERROR");
    }
}