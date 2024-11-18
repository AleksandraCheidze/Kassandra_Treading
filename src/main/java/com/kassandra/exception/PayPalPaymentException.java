package com.kassandra.exception;

public class PayPalPaymentException extends PaymentProcessingException {
    public PayPalPaymentException(String message) {
        super(message);
    }

    public PayPalPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
