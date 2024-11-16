package com.kassandra.service;

import com.kassandra.domain.OrderType;
import com.kassandra.exception.WalletException;
import com.kassandra.modal.Order;
import com.kassandra.modal.User;
import com.kassandra.modal.Wallet;
import com.kassandra.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletService{

    @Autowired
    private WalletRepository walletRepository;
    @Override
    public Wallet getUserWallet(User user) {
        Wallet wallet = walletRepository.findByUserId(user.getId());
        if (wallet == null){
            wallet = new Wallet();
            wallet.setUser(user);

        }
        return wallet;
    }

    @Override
    public Wallet addBalanceToWallet(Wallet wallet, Long money) throws WalletException {


        BigDecimal newBalance = wallet.getBalance().add(BigDecimal.valueOf(money));

//        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
//            throw new Exception("Insufficient funds for this transaction.");
//        }


        wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(money)));

        walletRepository.save(wallet);
        System.out.println("updated wallet - "+wallet);
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
    public Wallet walletToWalletTransfer(User sender, Wallet receiverWallet, Long amount) throws Exception {
        Wallet senderWallet = getUserWallet(sender);

        if (senderWallet.getBalance().compareTo(BigDecimal.valueOf(amount))<0) {
            throw new Exception("Insufficient balance: requested transfer of "
                    + amount + ", but current balance is "
                    + senderWallet.getBalance());
        }
        BigDecimal senderBalance = senderWallet.getBalance().subtract(BigDecimal
                .valueOf(amount));
        senderWallet.setBalance(senderBalance);
        walletRepository.save(senderWallet);

        BigDecimal receiverBalance = receiverWallet.getBalance().add(BigDecimal.valueOf(amount));
        receiverWallet.setBalance(receiverBalance);
        return senderWallet;
    }

    @Transactional
    @Override
    public Wallet payOrderPayment(Order order, User user) throws Exception {
        Wallet wallet = getUserWallet(user);

        if (order.getOrderType().equals(OrderType.BUY)){
            BigDecimal newBalance = wallet.getBalance().subtract(order.getPrice());
            if(newBalance.compareTo(order.getPrice())<0){
                throw new Exception("Insufficient funds for this transaction");
            }
            wallet.setBalance(newBalance);
        }
        else {
            BigDecimal newBalance = wallet.getBalance().add(order.getPrice());
            wallet.setBalance(newBalance);
        }
        walletRepository.save(wallet);
        return wallet;
    }
}
