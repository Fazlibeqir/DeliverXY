package com.deliverXY.backend.NewCode.common.util;

public final class SafeErrorMessages {

    private SafeErrorMessages() {
    }

    public static String paymentProviderMessage(String operation) {
        return operation + " failed";
    }
}
