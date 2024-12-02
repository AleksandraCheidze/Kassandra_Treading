package com.kassandra.service.impl;
import com.kassandra.config.PaypalConfig;
import com.kassandra.service.PaymentService;
import com.paypal.base.rest.PayPalRESTException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {


    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private PaypalConfig paypalConfig;

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
        PaymentOrder order=new PaymentOrder();
        order.setUser(user);
        order.setAmount(amount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(PaymentOrderStatus.PENDING);
        return paymentOrderRepository.save(order);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long id) throws Exception {
        Optional<PaymentOrder> optionalPaymentOrder=paymentOrderRepository.findById(id);
        if(optionalPaymentOrder.isEmpty()){
            throw new Exception("payment order not found with id "+id);
        }
        return optionalPaymentOrder.get();
    }


    @Override
    public Boolean proccedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws PayPalRESTException {
        if (paymentOrder.getStatus() == null) {
            paymentOrder.setStatus(PaymentOrderStatus.PENDING);
        }

        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
            if (paymentOrder.getPaymentMethod().equals(PaymentMethod.PAYPAL)) {
                APIContext apiContext = new APIContext(clientId, clientSecret, "sandbox");
                System.out.println("Attempting to retrieve payment with ID: " + paymentId);

                Payment payment = Payment.get(apiContext, paymentId);
                System.out.println("Retrieved payment state: " + payment.getState());

                String status = payment.getState();

                if ("approved".equals(status)) {
                    paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                    paymentOrderRepository.save(paymentOrder);
                    return true;
                }
                if ("created".equals(status)) {
                    System.out.println("Payment is in 'created' state. Trying to execute the payment.");

                    PaymentExecution paymentExecution = new PaymentExecution();
                    paymentExecution.setPayerId(payment.getPayer().getPayerInfo().getPayerId());
                    Payment executedPayment = payment.execute(apiContext, paymentExecution);

                    status = executedPayment.getState();

                    if ("approved".equals(status)) {
                        paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                        paymentOrderRepository.save(paymentOrder);
                        System.out.println("Payment approved after execution, order status updated to SUCCESS");
                        return true;
                    }
                }

                paymentOrder.setStatus(PaymentOrderStatus.FAILED);
                paymentOrderRepository.save(paymentOrder);
                System.out.println("Payment failed, order status updated to FAILED");
                return false;
            }

            paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
            paymentOrderRepository.save(paymentOrder);
            return true;
        }

        return false;
    }



    @Override
    public PaymentResponse createPaypalPaymentLink(User user, Long Amount, Long orderId) throws PayPalRESTException {

        APIContext apiContext = paypalConfig.apiContext();

        Amount totalAmount = new Amount();
        totalAmount.setCurrency("USD");
        totalAmount.setTotal(String.valueOf(Amount));

        Transaction transaction = new Transaction();
        transaction.setAmount(totalAmount);
        transaction.setDescription("Order #" + orderId);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Настройка URL-ов возврата и отмены
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:5173/wallet/cancel");
        redirectUrls.setReturnUrl("http://localhost:5173/wallet?order_id="+ orderId);

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(new Payer().setPaymentMethod("paypal"));
        payment.setTransactions(transactions);
        payment.setRedirectUrls(redirectUrls);

        Payment createdPayment = payment.create(apiContext);

        String approvalLink = createdPayment.getLinks().stream()
                .filter(link -> "approval_url".equals(link.getRel()))
                .findFirst()
                .map(Links::getHref)
                .orElseThrow(() -> new RuntimeException("No approval URL found"));

        PaymentResponse res = new PaymentResponse();
        res.setPayment_url(approvalLink);
        return res;
    }


    @Override
    public PaymentResponse createStripePaymentLink(User user, Long amount,Long orderId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/wallet?order_id="+orderId)
                .setCancelUrl("http://localhost:5173/payment/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount*100)
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

        Session session = Session.create(params);

        System.out.println("session _____ " + session);

        PaymentResponse res = new PaymentResponse();
        res.setPayment_url(session.getUrl());

        return res;
    }
}
