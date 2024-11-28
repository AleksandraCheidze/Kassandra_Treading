package com.kassandra.service;

import com.kassandra.domain.PaymentMethod;
import com.kassandra.modal.PaymentOrder;
import com.kassandra.modal.User;
import com.kassandra.response.PaymentResponse;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.exception.StripeException;

public interface PaymentService {

    PaymentOrder createOrder(User user, Long amount, PaymentMethod paymentMethod);
    PaymentOrder getPaymentOrderById(Long id) throws Exception;


    Boolean proccedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws PayPalRESTException;

    PaymentResponse createPaypalPaymentLink(User user, Long amount, Long orderId) throws PayPalRESTException;

    PaymentResponse createStripePaymentLink(User user, Long amount, Long orderId) throws StripeException;


}
