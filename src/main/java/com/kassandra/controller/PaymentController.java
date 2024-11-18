package com.kassandra.controller;

import com.kassandra.domain.PaymentMethod;
import com.kassandra.modal.PaymentOrder;
import com.kassandra.modal.User;
import com.kassandra.response.PaymentResponse;
import com.kassandra.service.PaymentService;
import com.kassandra.service.UserService;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/api/payment/{paymentMethod}/amount/{amount}")
    public ResponseEntity<PaymentResponse> paymentHandler(
            @PathVariable PaymentMethod paymentMethod,
            @PathVariable Long amount,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);

        PaymentOrder order = paymentService.createOrder(user, amount, paymentMethod);

        PaymentResponse paymentResponse;

        switch (paymentMethod) {
            case PAYPAL:
                try {
                    paymentResponse = paymentService.createPaypalPaymentLink(user, amount, order.getId());
                } catch (PayPalRESTException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new PaymentResponse("PayPal error: " + e.getMessage()));
                }
                break;

            case STRIPE:
                try {
                    paymentResponse = paymentService.createStripePaymentLink(user, amount, order.getId());
                } catch (StripeException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new PaymentResponse("Stripe error: " + e.getMessage()));
                }
                break;

            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new PaymentResponse("Unsupported payment method: " + paymentMethod));
        }

        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }
}
