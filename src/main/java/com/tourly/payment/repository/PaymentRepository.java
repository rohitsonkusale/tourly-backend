package com.tourly.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourly.booking.entity.Booking;
import com.tourly.payment.entity.Payment;
import com.tourly.payment.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String orderId);

    List<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findFirstByBookingIdOrderByCreatedAtDesc(Long bookingId);

    Optional<Payment> findFirstByPaymentStageIdOrderByCreatedAtDesc(Long paymentStageId);

    List<Payment> findByBooking(Booking booking);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    List<Payment> findByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    // =====================================
    // ADMIN DASHBOARD STATS
    // =====================================
    long countByStatus(PaymentStatus status);

    long countByStatusIn(List<PaymentStatus> statuses);
}
