package com.tourly.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
 import java.util.Optional;
import com.tourly.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String orderId);
}