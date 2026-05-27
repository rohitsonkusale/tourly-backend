package com.tourly.payment.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.payment.dto.request.RefundRequest;
import com.tourly.payment.dto.response.RefundResponse;
import com.tourly.payment.entity.Payment;
import com.tourly.payment.enums.PaymentStatus;
import com.tourly.payment.repository.PaymentRepository;
import com.tourly.payment.service.RefundService;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.repository.TripRepository;

@Service
public class RefundServiceImpl implements RefundService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final RazorpayClient razorpayClient;

    public RefundServiceImpl(BookingRepository bookingRepository,
                             PaymentRepository paymentRepository,
                             TripRepository tripRepository,
                             UserRepository userRepository,
                             RazorpayClient razorpayClient) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.razorpayClient = razorpayClient;
    }

    @Override
    @Transactional
    public RefundResponse processFullRefund(Long bookingId, RefundRequest request, String userEmail) {

        // 1. Get logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // 2. Load booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // 3. Ownership check (traveler only)
        if (booking.getTraveler() == null || booking.getTraveler().getId() == null) {
            throw new BadRequestException("Booking traveler not found");
        }

        if (!booking.getTraveler().getId().equals(user.getId())) {
            throw new BadRequestException("You can only refund your own booking");
        }

        // 4. Booking status check
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED bookings can be refunded");
        }

        // 5. Load payment by booking
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking id: " + bookingId));

        // 6. Payment status check
        if (payment.getStatus() == PaymentStatus.REFUND_PENDING) {
            throw new BadRequestException("Refund is already in progress for this booking");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException("Payment is already refunded");
        }

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new BadRequestException("Only CAPTURED payments can be refunded");
        }

        // 7. Prevent double refund using refund id
        if (payment.getRazorpayRefundId() != null && !payment.getRazorpayRefundId().isBlank()) {
            throw new BadRequestException("Refund already processed for this payment");
        }

        // 8. Validate Razorpay payment id exists
        if (payment.getRazorpayPaymentId() == null || payment.getRazorpayPaymentId().isBlank()) {
            throw new BadRequestException("Razorpay payment id is missing, refund cannot be processed");
        }

        // 9. Trip validation
        Trip trip = booking.getTrip();
        if (trip == null) {
            throw new BadRequestException("Trip not found for this booking");
        }

        LocalDate tripStartDate = trip.getStartDate();
        if (tripStartDate == null) {
            throw new BadRequestException("Trip start date is missing");
        }

        // Refund only before trip starts
        if (!tripStartDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Refund is allowed only before trip start date");
        }

        // 10. Mark refund pending before calling Razorpay
        payment.setStatus(PaymentStatus.REFUND_PENDING);
        paymentRepository.save(payment);

        try {
            // 11. Create full refund request in paise
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue());

            // Optional notes
            JSONObject notes = new JSONObject();
            notes.put("bookingId", booking.getId());
            notes.put("reason", request != null && request.getReason() != null && !request.getReason().isBlank()
                    ? request.getReason()
                    : "Full refund before trip start");
            refundRequest.put("notes", notes);

            // 12. Refund directly using Razorpay payment id
            Refund refund = razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            // 13. Update payment after successful refund
            payment.setRazorpayRefundId(refund.get("id").toString());
            payment.setRefundAmount(payment.getAmount());
            payment.setRefundProcessedAt(LocalDateTime.now());
            payment.setRefundReason(request != null ? request.getReason() : null);
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // 14. Update booking
            booking.setStatus(BookingStatus.CANCELLED);

            // IMPORTANT: Keep booking payment status in sync
            // Since your booking payment enum currently has PENDING / SUCESS / FAILED only,
            // for Refund V1 we mark it FAILED after refund (payment no longer active).
            booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.FAILED);

            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            // 15. Release seats by reducing bookedSeats
            Integer currentBookedSeats = trip.getBookedSeats() == null ? 0 : trip.getBookedSeats();
            Integer seatsToRelease = booking.getSeatsBooked() == null ? 0 : booking.getSeatsBooked();

            int updatedBookedSeats = currentBookedSeats - seatsToRelease;
            if (updatedBookedSeats < 0) {
                updatedBookedSeats = 0;
            }

            trip.setBookedSeats(updatedBookedSeats);
            tripRepository.save(trip);

            // 16. Build response
            RefundResponse response = new RefundResponse();
            response.setBookingId(booking.getId());
            response.setPaymentId(payment.getId());
            response.setRazorpayPaymentId(payment.getRazorpayPaymentId());
            response.setRazorpayRefundId(payment.getRazorpayRefundId());
            response.setRefundedAmount(payment.getRefundAmount());
            response.setPaymentStatus(payment.getStatus().name());
            response.setBookingStatus(booking.getStatus().name());
            response.setRefundReason(payment.getRefundReason());
            response.setRefundedAt(payment.getRefundProcessedAt());

            return response;

        } catch (Exception ex) {
            // rollback payment status if refund API fails
            payment.setStatus(PaymentStatus.CAPTURED);
            paymentRepository.save(payment);

            throw new BadRequestException("Refund failed: " + ex.getMessage());
        }
    }
}