package com.tourly.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourly.payment.entity.PaymentStage;

public interface PaymentStageRepository extends JpaRepository<PaymentStage, Long> {

    List<PaymentStage> findByBookingIdOrderByStageNumberAsc(Long bookingId);
}
