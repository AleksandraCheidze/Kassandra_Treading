package com.kassandra.service.impl;

import com.kassandra.domain.OrderType;
import com.kassandra.exception.WalletException;
import com.kassandra.modal.Order;
import com.kassandra.modal.User;
import com.kassandra.modal.Wallet;
import com.kassandra.repository.WalletRepository;
import com.kassandra.service.WalletService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;
    @Override
    public Wallet getUserWallet(User user) {
        Wallet wallet = walletRepository.findByUserId(user.getId());
        if (wallet == null){
            wallet = new Wallet();
            wallet.setUser(user);
            walletRepository.save(wallet);
        }
        return wallet;
    }

    @Override
    public Wallet addBalanceToWallet(Wallet wallet, Long money) {

        BigDecimal newBalance = wallet.getBalance().add(BigDecimal.valueOf(money));

        wallet.setBalance(newBalance);

        walletRepository.save(wallet);
        System.out.println("Updated wallet - " + wallet);

        return wallet;
    }



    @Override
    public Wallet findWalletById(Long id) throws Exception {
        Optional<Wallet> wallet = walletRepository.findById(id);
        if (wallet.isPresent()){
            return wallet.get();
        }
        throw new Exception("wallet not found");
    }

    @Transactional
    @Override
    public Wallet walletToWalletTransfer(User sender, Wallet receiverWallet, Long amount) throws WalletException {
        // Получаем кошелек отправителя
        Wallet senderWallet = getUserWallet(sender);

        // Добавим логирование для проверки баланса
        System.out.println("Sender Wallet Balance: " + senderWallet.getBalance());
        System.out.println("Transfer Amount: " + amount);

        // Проверяем, что на кошельке отправителя достаточно средств
        BigDecimal senderBalance = senderWallet.getBalance();
        if (senderBalance == null) {
            senderBalance = BigDecimal.ZERO;
        }

        if (senderBalance.compareTo(BigDecimal.valueOf(amount)) < 0) {
            // Добавим логирование перед выбрасыванием исключения
            System.out.println("Insufficient balance. Sender balance: " + senderBalance + ", Transfer amount: " + amount);
            throw new WalletException("Insufficient balance...");
        }

        // Вычитаем сумму с баланса отправителя
        senderBalance = senderBalance.subtract(BigDecimal.valueOf(amount));
        senderWallet.setBalance(senderBalance);
        walletRepository.save(senderWallet);

        // Добавляем сумму на баланс получателя
        BigDecimal receiverBalance = receiverWallet.getBalance();
        if (receiverBalance == null) {
            receiverBalance = BigDecimal.ZERO;
        }

        receiverBalance = receiverBalance.add(BigDecimal.valueOf(amount));
        receiverWallet.setBalance(receiverBalance);
        walletRepository.save(receiverWallet);

        return senderWallet;
    }


    @Transactional
    @Override
    public Wallet payOrderPayment(Order order, User user) throws Exception {
        Wallet wallet = getUserWallet(user);

        if (order.getOrderType().equals(OrderType.BUY)) {
            if (wallet.getBalance().compareTo(order.getPrice()) < 0) {
                throw new Exception("Insufficient funds for this transaction");
            }
            wallet.setBalance(wallet.getBalance().subtract(order.getPrice()));
        } else {
            wallet.setBalance(wallet.getBalance().add(order.getPrice()));
        }
        walletRepository.save(wallet);
        return wallet;
    }
}
