package com.kassandra.response;

import lombok.Data;

@Data
public class PaymentResponse {
    private String payment_url;
    private String message;

    public PaymentResponse(String message) {
        this.message = message;
    }

    // Можете добавить конструктор по умолчанию, если нужно
    public PaymentResponse() {}
}
