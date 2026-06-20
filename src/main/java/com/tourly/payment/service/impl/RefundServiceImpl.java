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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RefundServiceImpl.class);

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

        // 10. Create Refund record in PENDING state (awaiting admin approval)
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

        log.info("Refund request submitted (awaiting admin approval). bookingId={}, paymentId={}, amount={}, userId={}",
                bookingId, payment.getId(), payment.getAmount(), user.getId());

        // 11. Build response — no Razorpay call, refund pending admin approval
        RefundResponse response = new RefundResponse();
        response.setBookingId(booking.getId());
        response.setPaymentId(payment.getId());
        response.setRazorpayPaymentId(payment.getRazorpayPaymentId());
        response.setRazorpayRefundId(null);
        response.setRefundedAmount(refundEntity.getRefundAmount());
        response.setPaymentStatus(payment.getStatus().name());
        response.setBookingStatus(booking.getStatus().name());
        response.setRefundStatus(RefundStatus.PENDING.name());
        response.setRefundReason(refundEntity.getReason());
        response.setRefundedAt(null);

        return response;
    }

    // =========================================================================
    // AUTO-CANCELLATION REFUND
    // Called when a booking is cancelled due to missed payment deadlines.
    // Applies the Roamaya cancellation policy refund tiers based on
    // days remaining before departure.
    //
    // Policy:
    //   > 30 days before departure → 90% refund
    //   15–30 days → 50% refund
    //   7–14 days → 25% refund
    //   < 7 days → 0% refund (no refund)
    // =========================================================================
    @Override
    @Transactional
    public void initiateAutoCancellationRefund(Long bookingId, String reason) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for refund: " + bookingId));

        // Only refund if something was actually paid
        if (booking.getAmountPaid() == null || booking.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("No refund needed for bookingId={} — no amount paid", bookingId);
            return;
        }

        // Calculate refund percentage based on days before departure
        Trip trip = booking.getTrip();
        if (trip == null || trip.getStartDate() == null) {
            log.warn("Cannot calculate refund — trip or start date missing. bookingId={}", bookingId);
            return;
        }

        long daysBeforeDeparture = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), trip.getStartDate());
        BigDecimal refundPercentage = calculateRefundPercentage(daysBeforeDeparture);

        if (refundPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("No refund eligible for bookingId={}. daysBeforeDeparture={}", bookingId, daysBeforeDeparture);
            return;
        }

        // Calculate total refund amount
        BigDecimal totalRefundAmount = booking.getAmountPaid()
                .multiply(refundPercentage)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        log.info("Creating refund request (pending admin approval). bookingId={}, amountPaid={}, refundPercentage={}%, refundAmount={}",
                bookingId, booking.getAmountPaid(), refundPercentage, totalRefundAmount);

        // Find all PAID payments for this booking
        List<Payment> paidPayments = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.PAID);

        if (paidPayments.isEmpty()) {
            log.warn("No paid payments found for bookingId={}", bookingId);
            return;
        }

        // Create PENDING refund records for each paid payment (awaiting admin approval)
        BigDecimal remainingRefund = totalRefundAmount;

        for (Payment payment : paidPayments) {
            if (remainingRefund.compareTo(BigDecimal.ZERO) <= 0) break;

            // Refund amount for this payment (proportional or whatever is left)
            BigDecimal paymentRefundAmount = payment.getAmount().min(remainingRefund);

            // Check if refund already exists for this payment
            boolean refundExists = refundRepository.existsByPaymentIdAndStatusIn(
                    payment.getId(),
                    List.of(RefundStatus.PENDING, RefundStatus.APPROVED, RefundStatus.PROCESSED)
            );
            if (refundExists) {
                log.info("Refund already exists for paymentId={}, skipping", payment.getId());
                continue;
            }

            // Create refund record in PENDING status (requires admin approval)
            Refund refund = new Refund();
            refund.setBooking(booking);
            refund.setPayment(payment);
            refund.setOriginalAmount(payment.getAmount());
            refund.setRefundAmount(paymentRefundAmount);
            refund.setRefundType(paymentRefundAmount.compareTo(payment.getAmount()) >= 0
                    ? RefundType.FULL : RefundType.PARTIAL);
            refund.setStatus(RefundStatus.PENDING);
            refund.setReason(reason);
            refund.setAdminNotes("Auto-generated. Refund tier: " + refundPercentage + "% (" + daysBeforeDeparture + " days before departure)");
            refund.setRequestedAt(LocalDateTime.now());
            refundRepository.save(refund);

            remainingRefund = remainingRefund.subtract(paymentRefundAmount);

            log.info("Refund request created (PENDING admin approval). paymentId={}, refundAmount={}",
                    payment.getId(), paymentRefundAmount);
        }

        // Update booking payment status to indicate refund is pending
        booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.REFUNDED);
        bookingRepository.save(booking);

        log.info("Refund request(s) submitted for bookingId={}. Total refund amount: {} (awaiting admin approval)",
                bookingId, totalRefundAmount);
    }

    /**
     * Admin approves and processes a pending refund via Razorpay.
     */
    @Override
    @Transactional
    public void approveAndProcessRefund(Long refundId, Long adminUserId) {

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new BadRequestException("Refund is not in PENDING status. Current: " + refund.getStatus());
        }

        Payment payment = refund.getPayment();
        if (payment == null) {
            throw new BadRequestException("No payment linked to this refund");
        }

        if (payment.getRazorpayPaymentId() == null || payment.getRazorpayPaymentId().isBlank()) {
            throw new BadRequestException("Cannot process refund — Razorpay payment ID missing");
        }

        // Mark as APPROVED
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        refund.setProcessedBy(admin);
        refund.setStatus(RefundStatus.APPROVED);
        refundRepository.save(refund);

        // Process via Razorpay
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", refund.getRefundAmount().multiply(BigDecimal.valueOf(100)).intValue());

            JSONObject notes = new JSONObject();
            notes.put("bookingId", refund.getBooking().getId());
            notes.put("reason", refund.getReason());
            notes.put("approvedBy", adminUserId);
            refundRequest.put("notes", notes);

            com.razorpay.Refund razorpayRefund = razorpayClient.payments.refund(
                    payment.getRazorpayPaymentId(), refundRequest);

            refund.setRazorpayRefundId(razorpayRefund.get("id").toString());
            refund.setStatus(RefundStatus.PROCESSED);
            refund.setProcessedAt(LocalDateTime.now());
            refundRepository.save(refund);

            // Mark payment as refunded
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            log.info("Refund approved and processed. refundId={}, paymentId={}, amount={}, razorpayRefundId={}",
                    refundId, payment.getId(), refund.getRefundAmount(), refund.getRazorpayRefundId());

        } catch (Exception ex) {
            refund.setStatus(RefundStatus.FAILED);
            refund.setAdminNotes(refund.getAdminNotes() + " | Razorpay failed: " + ex.getMessage());
            refundRepository.save(refund);

            log.error("Razorpay refund failed for refundId={}: {}", refundId, ex.getMessage());
            throw new BadRequestException("Refund processing failed: " + ex.getMessage());
        }
    }

    /**
     * Admin rejects a pending refund.
     */
    @Override
    @Transactional
    public void rejectRefund(Long refundId, Long adminUserId, String rejectionReason) {

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new BadRequestException("Refund is not in PENDING status. Current: " + refund.getStatus());
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        refund.setProcessedBy(admin);
        refund.setStatus(RefundStatus.REJECTED);
        refund.setAdminNotes(rejectionReason);
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);

        log.info("Refund rejected. refundId={}, adminId={}, reason={}", refundId, adminUserId, rejectionReason);
    }

    /**
     * Determines refund percentage based on Roamaya cancellation policy.
     *
     * > 30 days before departure → 90%
     * 15–30 days → 50%
     * 7–14 days → 25%
     * < 7 days → 0%
     */
    private BigDecimal calculateRefundPercentage(long daysBeforeDeparture) {
        if (daysBeforeDeparture > 30) {
            return new BigDecimal("90");
        } else if (daysBeforeDeparture >= 15) {
            return new BigDecimal("50");
        } else if (daysBeforeDeparture >= 7) {
            return new BigDecimal("25");
        } else {
            return BigDecimal.ZERO;
        }
    }
}
