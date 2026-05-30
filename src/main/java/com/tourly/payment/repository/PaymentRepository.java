package com.tourly.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourly.booking.entity.Booking;
import com.tourly.payment.entity.Payment;
import com.tourly.payment.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String orderId);

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByBooking(Booking booking);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    boolean existsByRazorpayRefundId(String razorpayRefundId);

    // =====================================
    // ADMIN DASHBOARD STATS
    // =====================================
    long countByStatus(PaymentStatus status);

    long countByStatusIn(java.util.List<PaymentStatus> statuses);
}