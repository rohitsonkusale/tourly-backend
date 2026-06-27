package com.tourly.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourly.payment.entity.Refund;
import com.tourly.payment.enums.RefundStatus;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByBookingId(Long bookingId);

    Optional<Refund> findByPaymentId(Long paymentId);

    boolean existsByPaymentIdAndStatusIn(Long paymentId, List<RefundStatus> statuses);

    long countByStatus(RefundStatus status);

    List<Refund> findByStatusOrderByRequestedAtDesc(RefundStatus status);

    List<Refund> findAllByOrderByRequestedAtDesc();
}
