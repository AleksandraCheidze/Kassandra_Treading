package com.kassandra.service.impl;

import com.kassandra.domain.WithdrawalStatus;
import com.kassandra.modal.User;
import com.kassandra.modal.Withdrawal;
import com.kassandra.repository.WithdrawalRepository;
import com.kassandra.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    @Autowired
    private WithdrawalRepository withdrawalRepository;


    @Override
    public Withdrawal requestWithdrawal(Long amount,User user) {
        Withdrawal withdrawal=new Withdrawal();
        withdrawal.setAmount(amount);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        withdrawal.setDate(LocalDateTime.now());
        withdrawal.setUser(user);
        return withdrawalRepository.save(withdrawal);
    }

    @Override
    public Withdrawal procedWithdrawal(Long withdrawalId,boolean accept) throws Exception {
        Optional<Withdrawal> withdrawalOptional=withdrawalRepository.findById(withdrawalId);

        if(withdrawalOptional.isEmpty()){
            throw new Exception("withdrawal id is wrong...");
        }

        Withdrawal withdrawal=withdrawalOptional.get();


        withdrawal.setDate(LocalDateTime.now());

        if(accept){
            withdrawal.setStatus(WithdrawalStatus.SUCCESS);
        }
        else{
            withdrawal.setStatus(WithdrawalStatus.DECLINE);
        }

        return withdrawalRepository.save(withdrawal);
    }

    @Override
    public List<Withdrawal> getUsersWithdrawalHistory(User user) {
        return withdrawalRepository.findByUserId(user.getId());
    }

    @Override
    public List<Withdrawal> getAllWithdrawalRequest() {
        return withdrawalRepository.findAll();
    }
}
