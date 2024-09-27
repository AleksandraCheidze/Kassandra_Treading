package com.kassandra.service;

import com.kassandra.modal.Order;
import com.kassandra.modal.User;
import com.kassandra.modal.Wallet;

public interface WalletService {
    Wallet getUserWallet(User user);

    Wallet addBalance(Wallet wallet, Long money);

    Wallet findWalletById(Long id) throws Exception;

    Wallet walletToWalletTransfer(User sender, Wallet receiverWallet, Long amount) throws Exception;

    Wallet payOrderPayment(Order order, User user) throws Exception;


}
