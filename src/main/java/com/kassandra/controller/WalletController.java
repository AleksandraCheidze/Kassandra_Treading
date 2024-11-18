package com.kassandra.controller;

import com.kassandra.domain.WalletTransactionType;
import com.kassandra.modal.*;
import com.kassandra.response.PaymentResponse;
import com.kassandra.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WalletController {

    @Autowired
    private WalletService walleteService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/api/wallet")
    public ResponseEntity<?> getUserWallet(@RequestHeader("Authorization")String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Wallet wallet = walleteService.getUserWallet(user);
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    @GetMapping("/api/wallet/transactions")
    public ResponseEntity<List<WalletTransaction>> getWalletTransaction(
            @RequestHeader("Authorization")String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Wallet wallet = walleteService.getUserWallet(user);
        List<WalletTransaction> transactions = walletTransactionService.getTransactions(wallet, null);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PutMapping("/api/wallet/deposit/amount/{amount}")
    public ResponseEntity<PaymentResponse> depositMoney(@RequestHeader("Authorization")String jwt,
                                                        @PathVariable Long amount) {
        PaymentResponse res = new PaymentResponse();
        try {
            User user = userService.findUserProfileByJwt(jwt);
            Wallet wallet = walleteService.getUserWallet(user);
            walleteService.addBalanceToWallet(wallet, amount);
            res.setMessage("Deposit successful");
            res.setPayment_url("deposited successfully");
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            res.setMessage("Error during deposit: " + e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/api/wallet/deposit")
    public ResponseEntity<Wallet> addMoneyToWallet(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(name = "order_id") Long orderId,
            @RequestParam(name = "payment_id") String paymentId) {
        PaymentResponse res = new PaymentResponse();
        try {
            User user = userService.findUserProfileByJwt(jwt);
            Wallet wallet = walleteService.getUserWallet(user);
            PaymentOrder order = paymentService.getPaymentOrderById(orderId);
            Boolean status = paymentService.ProccedPaymentOrder(order, paymentId);
            if (status) {
                wallet = walleteService.addBalanceToWallet(wallet, order.getAmount());
                res.setMessage("Deposit successful");
            } else {
                res.setMessage("Payment failed");
            }
            res.setPayment_url("Deposit success");
            return new ResponseEntity<>(wallet, HttpStatus.OK);
        } catch (Exception e) {
            res.setMessage("Error during deposit: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/api/wallet/{walletId}/transfer")
    public ResponseEntity<Wallet> walletToWalletTransfer(@RequestHeader("Authorization")String jwt,
                                                         @PathVariable Long walletId,
                                                         @RequestBody WalletTransaction req) {
        try {
            User senderUser = userService.findUserProfileByJwt(jwt);
            Wallet reciverWallet = walleteService.findWalletById(walletId);
            Wallet wallet = walleteService.walletToWalletTransfer(senderUser, reciverWallet, req.getAmount());
            walletTransactionService.createTransaction(
                    wallet,
                    WalletTransactionType.WALLET_TRANSFER, reciverWallet.getId().toString(),
                    req.getPurpose(),
                    -req.getAmount()
            );
            return new ResponseEntity<>(wallet, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/api/wallet/order/{orderId}/pay")
    public ResponseEntity<Wallet> payOrderPayment(@PathVariable Long orderId,
                                                  @RequestHeader("Authorization")String jwt) {
        try {
            User user = userService.findUserProfileByJwt(jwt);
            Order order = orderService.getOrderById(orderId);
            Wallet wallet = walleteService.payOrderPayment(order, user);
            return new ResponseEntity<>(wallet, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
