package com.tourly.payment.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.razorpay.RazorpayClient;
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
import com.tourly.payment.entity.Refund;
import com.tourly.payment.enums.PaymentStatus;
import com.tourly.payment.enums.RefundStatus;
import com.tourly.payment.enums.RefundType;
import com.tourly.payment.repository.PaymentRepository;
import com.tourly.payment.repository.RefundRepository;
import com.tourly.payment.service.RefundService;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.repository.TripRepository;

@Service
public class RefundServiceImpl implements RefundService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final RazorpayClient razorpayClient;

    public RefundServiceImpl(BookingRepository bookingRepository,
                             PaymentRepository paymentRepository,
                             RefundRepository refundRepository,
                             TripRepository tripRepository,
                             UserRepository userRepository,
                             RazorpayClient razorpayClient) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
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

        // 5. Load the most recent PAID payment for this booking
        Payment payment = paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking id: " + bookingId));

        // 6. Payment status check
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException("Payment is already refunded");
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Only PAID payments can be refunded");
        }

        // 7. Prevent double refund — check if a refund already exists for this payment
        boolean refundExists = refundRepository.existsByPaymentIdAndStatusIn(
                payment.getId(),
                List.of(RefundStatus.PENDING, RefundStatus.APPROVED, RefundStatus.PROCESSED)
        );
        if (refundExists) {
            throw new BadRequestException("Refund already processed or pending for this payment");
        }

        // 8. Validate Razorpay payment id exists
        if (payment.getRazorpayPaymentId() == null || payment.getRazorpayPaymentId().isBlank()) {
            throw new BadRequestException("Razorpay payment id is missing, refund cannot be processed");
        }

        // 9. Trip validation — refund only before trip starts
        Trip trip = booking.getTrip();
        if (trip == null) {
            throw new BadRequestException("Trip not found for this booking");
        }

        LocalDate tripStartDate = trip.getStartDate();
        if (tripStartDate == null) {
            throw new BadRequestException("Trip start date is missing");
        }

        if (!tripStartDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Refund is allowed only before trip start date");
        }

        // 10. Create Refund record (PENDING state)
        Refund refundEntity = new Refund();
        refundEntity.setBooking(booking);
        refundEntity.setPayment(payment);
        refundEntity.setRequestedBy(user);
        refundEntity.setOriginalAmount(payment.getAmount());
        refundEntity.setRefundAmount(payment.getAmount());
        refundEntity.setRefundType(RefundType.FULL);
        refundEntity.setStatus(RefundStatus.PENDING);
        refundEntity.setReason(request != null ? request.getReason() : "Full refund before trip start");
        refundEntity.setRequestedAt(LocalDateTime.now());
        refundRepository.save(refundEntity);

        try {
            // 11. Create full refund request via Razorpay (amount in paise)
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue());

            JSONObject notes = new JSONObject();
            notes.put("bookingId", booking.getId());
            notes.put("reason", refundEntity.getReason());
            refundRequest.put("notes", notes);

            // 12. Call Razorpay refund API
            com.razorpay.Refund razorpayRefund = razorpayClient.payments.refund(
                    payment.getRazorpayPaymentId(), refundRequest);

            // 13. Update Refund entity with Razorpay response
            refundEntity.setRazorpayRefundId(razorpayRefund.get("id").toString());
            refundEntity.setStatus(RefundStatus.PROCESSED);
            refundEntity.setProcessedAt(LocalDateTime.now());
            refundRepository.save(refundEntity);

            // 14. Update Payment status
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // 15. Update Booking
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.REFUNDED);
            booking.setCancelledAt(LocalDateTime.now());
            bookingRepository.save(booking);

            // 16. Release seats
            Integer currentBookedSeats = trip.getBookedSeats() == null ? 0 : trip.getBookedSeats();
            Integer seatsToRelease = booking.getSeatsBooked() == null ? 0 : booking.getSeatsBooked();
            int updatedBookedSeats = Math.max(0, currentBookedSeats - seatsToRelease);
            trip.setBookedSeats(updatedBookedSeats);
            tripRepository.save(trip);

            // 17. Build response
            RefundResponse response = new RefundResponse();
            response.setBookingId(booking.getId());
            response.setPaymentId(payment.getId());
            response.setRazorpayPaymentId(payment.getRazorpayPaymentId());
            response.setRazorpayRefundId(refundEntity.getRazorpayRefundId());
            response.setRefundedAmount(refundEntity.getRefundAmount());
            response.setPaymentStatus(payment.getStatus().name());
            response.setBookingStatus(booking.getStatus().name());
            response.setRefundReason(refundEntity.getReason());
            response.setRefundedAt(refundEntity.getProcessedAt());

            return response;

        } catch (Exception ex) {
            // Rollback: mark refund as failed, restore payment status
            refundEntity.setStatus(RefundStatus.FAILED);
            refundRepository.save(refundEntity);

            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            throw new BadRequestException("Refund failed: " + ex.getMessage());
        }
    }
}
