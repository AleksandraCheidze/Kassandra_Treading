package com.kassandra.repository;

import com.kassandra.modal.PaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails, Long> {



    PaymentDetails getPaymentDetailsByUserId(Long id);
}
