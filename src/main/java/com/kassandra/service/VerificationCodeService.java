package com.kassandra.service;

import com.kassandra.domain.VerificationType;
import com.kassandra.modal.User;
import com.kassandra.modal.VerificationCode;

public interface VerificationCodeService {
    VerificationCode sendVerificationCode(User user, VerificationType verificationType);

    VerificationCode getVerificationCodeById(Long id) throws Exception;

    VerificationCode getVerificationCodeByUser(Long userId);


    void deleteVerificationCode(VerificationCode verificationCode);

    void deleteVerificationCodeById(VerificationCode verificationCode);
}
