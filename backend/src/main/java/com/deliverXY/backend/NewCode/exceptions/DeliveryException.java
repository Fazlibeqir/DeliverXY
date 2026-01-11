package com.deliverXY.backend.NewCode.exceptions;

public class DeliveryException extends BaseException {
    public DeliveryException(String message) {
        super(message, "DELIVERY_ERROR");
    }
}