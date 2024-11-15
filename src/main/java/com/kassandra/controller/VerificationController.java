package com.kassandra.controller;

import com.kassandra.service.EmailService;
import com.kassandra.service.UserService;
import com.kassandra.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController {
    private final VerificationCodeService verificationService;
    private final UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    public VerificationController(VerificationCodeService verificationService, UserService userService) {
        this.verificationService = verificationService;
        this.userService = userService;
    }




}
