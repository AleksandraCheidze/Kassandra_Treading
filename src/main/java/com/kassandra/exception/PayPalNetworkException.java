package com.kassandra.exception;

public class PayPalNetworkException extends PaymentProcessingException {
    public PayPalNetworkException(String message) {
        super(message);
    }

    public PayPalNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
