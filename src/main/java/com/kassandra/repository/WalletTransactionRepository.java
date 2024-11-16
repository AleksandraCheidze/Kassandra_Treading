package com.kassandra.repository;

import com.kassandra.modal.Wallet;
import com.kassandra.modal.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletOrderByDateDesc(Wallet wallet);
}
