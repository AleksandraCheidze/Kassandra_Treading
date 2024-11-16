package com.kassandra.service;

import com.kassandra.domain.WalletTransactionType;
import com.kassandra.modal.Wallet;
import com.kassandra.modal.WalletTransaction;

import java.util.List;

public interface WalletTransactionService {
    WalletTransaction createTransaction(Wallet wallet,
                                        WalletTransactionType type,
                                        String transferId,
                                        String purpose,
                                        Long amount
    );

    List<WalletTransaction> getTransactions(Wallet wallet, WalletTransactionType type);

}

