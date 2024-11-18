package com.kassandra.service;
import com.paypal.base.rest.PayPalRESTException;
import com.kassandra.exception.PayPalAPIException;
import com.kassandra.exception.PayPalPaymentException;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.stripe.model.checkout.Session;
import com.kassandra.domain.PaymentMethod;
import com.kassandra.domain.PaymentOrderStatus;
import com.kassandra.modal.PaymentOrder;
import com.kassandra.modal.User;
import com.kassandra.repository.PaymentOrderRepository;
import com.kassandra.response.PaymentResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Override
    public PaymentOrder createOrder(User user, Long amount, PaymentMethod paymentMethod) {
        log.info("Creating payment order. User ID: {}, Amount: {}, Payment Method: {}", user.getId(), amount, paymentMethod);
        try {
            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setUser(user);
            paymentOrder.setAmount(amount);
            paymentOrder.setPaymentMethod(paymentMethod);
            paymentOrder.setStatus(PaymentOrderStatus.PENDING);
            PaymentOrder savedOrder = paymentOrderRepository.save(paymentOrder);
            log.info("Payment order created successfully. Order ID: {}", savedOrder.getId());
            return savedOrder;
        } catch (Exception e) {
            log.error("Error while creating payment order for User ID: {}. Error: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long id) throws Exception {
        return paymentOrderRepository.findById(id).orElseThrow(() -> {
            log.error("Payment order not found for ID: {}", id);
            return new Exception("Payment order not found");
        });
    }

    @Override
    public Boolean ProccedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws PayPalRESTException{
        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {

            if (paymentOrder.getPaymentMethod().equals(PaymentMethod.PAYPAL)) {
                APIContext apiContext = new APIContext(clientId, clientSecret, mode);

                try {
                    Payment payment = Payment.get(apiContext, paymentId);
                    String paymentState = payment.getState();
                    log.info("Retrieved payment details for Payment ID: {}. State: {}", paymentId, paymentState);

                    if ("approved".equalsIgnoreCase(paymentState)) {
                        paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                        log.info("Payment approved. Order ID: {}", paymentOrder.getId());
                    } else {
                        paymentOrder.setStatus(PaymentOrderStatus.FAILED);
                        log.info("Payment failed. Order ID: {}", paymentOrder.getId());
                    }

                    paymentOrderRepository.save(paymentOrder);
                    return "approved".equalsIgnoreCase(paymentState);

                } catch (PayPalRESTException e) {
                    log.error("PayPal API error while processing Payment ID: {}", paymentId, e);
                    handlePayPalError(e);
                } catch (Exception e) {
                    log.error("Unexpected error while processing Payment ID: {}", paymentId, e);
                }

                paymentOrder.setStatus(PaymentOrderStatus.FAILED);
                paymentOrderRepository.save(paymentOrder);
                return false;
            }
            paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
            paymentOrderRepository.save(paymentOrder);
            return true;
        }

        log.warn("Order is not in PENDING state. Skipping payment processing. Order ID: {}", paymentOrder.getId());
        return false;
    }



    private void handlePayPalError(PayPalRESTException e) {
        if (e.getMessage().contains("INTERNAL_SERVICE_ERROR")) {
            log.warn("Internal service error. Retrying after delay...");
            retryPaymentRequest();
        } else if (e.getMessage().contains("PAYMENT_NOT_FOUND")) {
            throw new PayPalPaymentException("Payment not found. Please check the payment ID.");
        } else if (e.getMessage().contains("TRANSACTION_REFUSED")) {
            throw new PayPalPaymentException("Payment was refused by PayPal. Please contact support.");
        } else if (e.getMessage().contains("INVALID_ACCOUNT")) {
            throw new PayPalAPIException("Invalid PayPal account. Please verify your account details.");
        } else {
            throw new PayPalAPIException("Unexpected error occurred during PayPal transaction.", e);
        }
    }

    private void retryPaymentRequest() {
        try {
            Thread.sleep(5000);
            log.info("Retrying PayPal request...");
        } catch (InterruptedException e) {
            log.error("Retry attempt interrupted", e);
        }
    }

    @Override
    public PaymentResponse createPaypalPaymentLink(User user, Long amount, Long orderId) throws PayPalRESTException {
        log.info("Creating PayPal payment link for User ID: {}, Amount: {}, Order ID: {}", user.getId(), amount, orderId);

        if (!"sandbox".equals(mode) && !"live".equals(mode)) {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }

        APIContext apiContext = new APIContext(clientId, clientSecret, mode);

        Amount paymentAmount = new Amount();
        paymentAmount.setCurrency("USD");
        paymentAmount.setTotal(amount.toString());

        Transaction transaction = new Transaction();
        transaction.setAmount(paymentAmount);
        transaction.setDescription("Top up wallet");

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:5173/payment/cancel");
        redirectUrls.setReturnUrl("http://localhost:5173/wallet?order_id=" + orderId);

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(Arrays.asList(transaction));
        payment.setRedirectUrls(redirectUrls);

        try {
            Payment createdPayment = payment.create(apiContext);

            String paymentLink = "";
            for (Links link : createdPayment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    paymentLink = link.getHref();
                    break;
                }
            }

            PaymentResponse res = new PaymentResponse();
            res.setPayment_url(paymentLink);
            log.info("PayPal payment link created: {}", paymentLink);
            return res;
        } catch (PayPalRESTException e) {
            log.error("PayPal API error while creating payment link", e);
            PaymentResponse res = new PaymentResponse("PayPal API error: " + e.getMessage());
            return res;
        }
    }


    @Override
    public PaymentResponse createStripePaymentLink(User user, Long amount, Long orderId) throws StripeException {
        log.info("Creating Stripe payment link for User ID: {}, Amount: {}, Order ID: {}", user.getId(), amount, orderId);

        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/wallet?ordr_id=" + orderId)
                .setCancelUrl("http://localhost:5173/payment/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(amount)
                                .setProductData(SessionCreateParams
                                        .LineItem
                                        .PriceData
                                        .ProductData
                                        .builder()
                                        .setName("Top up wallet")
                                        .build()
                                ).build()
                        ).build()
                ).build();

        try {
            Session session = Session.create(params);

            PaymentResponse res = new PaymentResponse();
            res.setPayment_url(session.getUrl());
            log.info("Stripe payment link created: {}", session.getUrl());
            return res;
        } catch (StripeException e) {
            log.error("Stripe API error while creating payment link", e);
            PaymentResponse res = new PaymentResponse("Stripe error: " + e.getMessage());
            return res;
        }
    }
}
