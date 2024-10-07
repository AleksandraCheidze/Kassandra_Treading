package com.kassandra.service;

import com.kassandra.modal.User;
import com.kassandra.modal.Withdrawal;

import java.util.List;

public interface WithdrawalService {

    Withdrawal requestyWithdrawal (Long amount, User user);

    Withdrawal procedWithwithdrawal(Long withdrawalId, boolean accept) throws Exception;

    List<Withdrawal> getUsersWithdrawalHistory(User user);

    List<Withdrawal> getAllWithdrawalRequest();
}
