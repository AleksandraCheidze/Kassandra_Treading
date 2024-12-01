package com.kassandra.service;

import com.kassandra.modal.Order;
import com.kassandra.modal.User;
import com.kassandra.modal.Wallet;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

public interface WalletService {
    Wallet getUserWallet(User user);

    @Transactional
    Wallet addBalanceToWallet(Wallet wallet, Long money);

    Wallet findWalletById(Long id) throws Exception;

    Wallet walletToWalletTransfer(User sender, Wallet receiverWallet, Long amount) throws Exception;

    Wallet payOrderPayment(Order order, User user) throws Exception;



}
