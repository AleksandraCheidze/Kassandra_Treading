package com.kassandra.exception;

public class PayPalAPIException extends PaymentProcessingException {
    public PayPalAPIException(String message) {
        super(message);
    }

    public PayPalAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
