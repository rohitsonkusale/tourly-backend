package com.tourly.payment.service.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.exception.UnauthorizedActionException;
import com.tourly.payment.dto.request.CreatePaymentRequest;
import com.tourly.payment.dto.request.VerifyPaymentRequest;
import com.tourly.payment.dto.response.PaymentResponse;
import com.tourly.payment.dto.response.UpcomingPaymentResponse;
import com.tourly.payment.entity.Payment;
import com.tourly.payment.entity.PaymentStage;
import com.tourly.payment.enums.PaymentStageStatus;
import com.tourly.payment.enums.PaymentStatus;
import com.tourly.payment.repository.PaymentRepository;
import com.tourly.payment.repository.PaymentStageRepository;
import com.tourly.payment.service.PaymentService;
import com.tourly.notification.service.PaymentNotificationService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final PaymentStageRepository paymentStageRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RazorpayClient razorpayClient;
    private final PaymentNotificationService paymentNotificationService;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentStageRepository paymentStageRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            RazorpayClient razorpayClient,
            PaymentNotificationService paymentNotificationService) {

        this.paymentRepository = paymentRepository;
        this.paymentStageRepository = paymentStageRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.razorpayClient = razorpayClient;
        this.paymentNotificationService = paymentNotificationService;
    }

    // =========================================
    // HELPER: GET CURRENT USER
    // =========================================
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedActionException("User is not authenticated");
        }

        String email = authentication.getName();

        if (email == null || email.trim().isEmpty() || "anonymousUser".equals(email)) {
            throw new UnauthorizedActionException("User is not authenticated");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // =========================================
    // HELPER: VALIDATE BOOKING OWNERSHIP
    // =========================================
    private void validateBookingOwnership(Booking booking, User currentUser) {
        if (booking.getTraveler() == null || booking.getTraveler().getId() == null) {
            throw new ResourceNotFoundException("Booking traveler not found");
        }

        if (!booking.getTraveler().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not authorized to pay for this booking");
        }
    }

    // =========================================
    // CREATE PAYMENT ORDER (STAGE-AWARE)
    // =========================================
    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        User currentUser = getCurrentUser();

        // 1. Load booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        validateBookingOwnership(booking, currentUser);

        // 2. Booking state validations
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay for a cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot pay for a completed booking");
        }
        if (booking.getPaymentStatus() == com.tourly.booking.enums.PaymentStatus.FULLY_PAID) {
            throw new BadRequestException("Booking is already fully paid");
        }

        // 3. Load and validate the payment stage
        PaymentStage stage = paymentStageRepository.findById(request.getStageId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment stage not found"));

        // Ensure stage belongs to this booking
        if (!stage.getBooking().getId().equals(booking.getId())) {
            throw new BadRequestException("Payment stage does not belong to this booking");
        }

        // Validate stage status
        if (stage.getStatus() == PaymentStageStatus.PAID) {
            throw new BadRequestException("This payment stage is already paid");
        }
        if (stage.getStatus() == PaymentStageStatus.CANCELLED) {
            throw new BadRequestException("This payment stage has been cancelled");
        }
        if (stage.getStatus() == PaymentStageStatus.OVERDUE) {
            throw new BadRequestException("This payment stage is overdue. Booking may be cancelled.");
        }

        // Validate stage is payable (must be PENDING or INVOICE_SENT)
        if (stage.getStatus() != PaymentStageStatus.PENDING
                && stage.getStatus() != PaymentStageStatus.INVOICE_SENT) {
            throw new BadRequestException("This payment stage is not currently payable");
        }

        // For non-immediate stages, check if invoice window is open
        if (!Boolean.TRUE.equals(stage.getIsImmediate())
                && stage.getStatus() == PaymentStageStatus.PENDING) {
            throw new BadRequestException(
                "Invoice window has not opened yet for this stage. Please wait until the invoice is sent.");
        }

        // Check deadline hasn't passed
        if (stage.getDeadlineAt() != null && stage.getDeadlineAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Payment deadline has passed for this stage");
        }

        // 4. Check for existing payment attempt on this stage
        BigDecimal amount = stage.getAmount();
        Payment existingPayment = paymentRepository
                .findFirstByPaymentStageIdOrderByCreatedAtDesc(stage.getId())
                .orElse(null);

        if (existingPayment != null) {
            // Already paid
            if (existingPayment.getStatus() == PaymentStatus.PAID) {
                throw new BadRequestException("This stage is already paid");
            }

            // Pending order exists — reuse it
            if (existingPayment.getStatus() == PaymentStatus.CREATED) {
                log.info("Existing pending order reused. bookingId={}, stageId={}, razorpayOrderId={}",
                        booking.getId(), stage.getId(), existingPayment.getRazorpayOrderId());

                PaymentResponse response = new PaymentResponse();
                response.setRazorpayOrderId(existingPayment.getRazorpayOrderId());
                response.setBookingId(booking.getId());
                response.setStageId(stage.getId());
                response.setStageNumber(stage.getStageNumber());
                response.setStageLabel(stage.getLabel());
                response.setAmount(amount);
                response.setStatus("ORDER_ALREADY_EXISTS");
                return response;
            }
        }

        // 5. Create Razorpay order
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // paisa
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + booking.getId() + "_stage_" + stage.getStageNumber());

            Order order = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = order.get("id").toString();

            Payment payment;

            // Retry after FAILED payment
            if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.FAILED) {
                payment = existingPayment;
                payment.setAmount(amount);
                payment.setRazorpayOrderId(razorpayOrderId);
                payment.setRazorpayPaymentId(null);
                payment.setRazorpaySignature(null);
                payment.setStatus(PaymentStatus.CREATED);
                payment.setAttemptNumber(existingPayment.getAttemptNumber() + 1);

                log.info("Retry payment for stage. bookingId={}, stageId={}, attempt={}, razorpayOrderId={}",
                        booking.getId(), stage.getId(), payment.getAttemptNumber(), razorpayOrderId);
            } else {
                // First payment attempt for this stage
                payment = new Payment();
                payment.setBooking(booking);
                payment.setPaymentStage(stage);
                payment.setAmount(amount);
                payment.setRazorpayOrderId(razorpayOrderId);
                payment.setStatus(PaymentStatus.CREATED);

                log.info("New payment order created. bookingId={}, stageId={}, stageNumber={}, amount={}, razorpayOrderId={}",
                        booking.getId(), stage.getId(), stage.getStageNumber(), amount, razorpayOrderId);
            }

            paymentRepository.save(payment);

            PaymentResponse response = new PaymentResponse();
            response.setRazorpayOrderId(razorpayOrderId);
            response.setBookingId(booking.getId());
            response.setStageId(stage.getId());
            response.setStageNumber(stage.getStageNumber());
            response.setStageLabel(stage.getLabel());
            response.setAmount(amount);
            response.setStatus("ORDER_CREATED");
            return response;

        } catch (BadRequestException | ResourceNotFoundException | UnauthorizedActionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create Razorpay order. bookingId={}, stageId={}, error={}",
                    booking.getId(), stage.getId(), e.getMessage(), e);
            throw new BadRequestException("Unable to create payment order");
        }
    }

    // =========================================
    // VERIFY PAYMENT (STAGE-AWARE)
    // =========================================
    @Override
    @Transactional
    public void verifyPayment(VerifyPaymentRequest request) {

        // 1. Find the payment by Razorpay order ID
        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Idempotency: already paid
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Payment verification skipped (already paid). paymentId={}, razorpayOrderId={}",
                    payment.getId(), request.getRazorpayOrderId());
            return;
        }

        Booking booking = payment.getBooking();
        if (booking == null) {
            throw new ResourceNotFoundException("Booking not found for payment");
        }

        PaymentStage stage = payment.getPaymentStage();
        if (stage == null) {
            throw new ResourceNotFoundException("Payment stage not found for payment");
        }

        // 2. Booking state checks
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Booking is already completed");
        }

        // 3. Verify Razorpay signature
        String data = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        String expectedSignature = hmacSHA256(data, razorpaySecret);

        log.debug("Payment verification: data='{}', expectedSig='{}', receivedSig='{}'",
                data, expectedSignature, request.getRazorpaySignature());

        if (!expectedSignature.equals(request.getRazorpaySignature())) {
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid signature");
            paymentRepository.save(payment);

            log.warn("Payment verification failed: invalid signature. bookingId={}, stageId={}, razorpayOrderId={}",
                    booking.getId(), stage.getId(), request.getRazorpayOrderId());

            throw new BadRequestException("Invalid payment signature");
        }

        // 4. Mark payment as PAID
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 5. Mark stage as PAID
        stage.setStatus(PaymentStageStatus.PAID);
        stage.setPaidAt(LocalDateTime.now());
        paymentStageRepository.save(stage);

        // 6. Update booking financials
        BigDecimal newAmountPaid = booking.getAmountPaid().add(stage.getAmount());
        booking.setAmountPaid(newAmountPaid);
        booking.setAmountPending(booking.getTotalPrice().subtract(newAmountPaid));
        booking.setUpdatedAt(LocalDateTime.now());

        // Determine booking payment status
        if (newAmountPaid.compareTo(booking.getTotalPrice()) >= 0) {
            booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.FULLY_PAID);
        } else {
            booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.PARTIALLY_PAID);
        }

        // First stage payment confirms the booking
        if (stage.getStageNumber() == 1 && booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setConfirmedAt(LocalDateTime.now());
            booking.setExpiresAt(null); // Clear expiry — booking is now confirmed
        }

        bookingRepository.save(booking);

        log.info("Payment verified. bookingId={}, stageId={}, stageNumber={}, amountPaid={}, totalPrice={}, paymentStatus={}",
                booking.getId(), stage.getId(), stage.getStageNumber(),
                newAmountPaid, booking.getTotalPrice(), booking.getPaymentStatus());

        // Notify traveler — Event 4: Payment Success
        String tripTitle = booking.getTrip() != null ? booking.getTrip().getTitle() : "Trip";
        paymentNotificationService.notifyPaymentSuccess(
                booking.getTraveler().getId(), booking.getId(),
                tripTitle, stage.getLabel(), stage.getAmount());

        // Notify host
        var host = booking.getTrip() != null ? booking.getTrip().getHost() : null;
        if (host == null && booking.getTrip() != null) host = booking.getTrip().getPlanner();
        if (host != null) {
            paymentNotificationService.notifyHostPaymentReceived(
                    host.getId(), booking.getId(),
                    booking.getTraveler().getFullName(),
                    tripTitle, stage.getLabel(), stage.getAmount());
        }
    }

    // =========================================
    // GET UPCOMING PAYMENTS FOR DASHBOARD
    // =========================================
    @Override
    public List<UpcomingPaymentResponse> getUpcomingPayments() {
        User currentUser = getCurrentUser();

        List<PaymentStageStatus> pendingStatuses = List.of(
                PaymentStageStatus.PENDING,
                PaymentStageStatus.INVOICE_SENT
        );

        List<PaymentStage> upcomingStages = paymentStageRepository
                .findUpcomingStagesForTraveler(currentUser.getId(), pendingStatuses);

        return upcomingStages.stream().map(stage -> {
            UpcomingPaymentResponse dto = new UpcomingPaymentResponse();
            dto.setStageId(stage.getId());
            dto.setBookingId(stage.getBooking().getId());
            dto.setBookingRef(stage.getBooking().getBookingRef());
            dto.setStageNumber(stage.getStageNumber());
            dto.setLabel(stage.getLabel());
            dto.setAmount(stage.getAmount());
            dto.setPercentage(stage.getPercentage());
            dto.setStatus(stage.getStatus() != null ? stage.getStatus().name() : null);
            dto.setDueDate(stage.getDueDate());
            dto.setDeadlineAt(stage.getDeadlineAt());
            dto.setIsImmediate(stage.getIsImmediate());

            // Trip context
            var trip = stage.getBooking().getTrip();
            if (trip != null) {
                dto.setTripId(trip.getId());
                dto.setTripTitle(trip.getTitle());
                dto.setTripStartDate(trip.getStartDate());
                var destination = trip.getDestination();
                if (destination != null) {
                    dto.setDestination(destination.getCity());
                }
            }

            return dto;
        }).toList();
    }

    // =========================================
    // HMAC SIGNATURE GENERATOR
    // =========================================
    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(2 * rawHmac.length);
            for (byte b : rawHmac) {
                String hexByte = Integer.toHexString(0xff & b);
                if (hexByte.length() == 1) {
                    hex.append('0');
                }
                hex.append(hexByte);
            }
            return hex.toString();
        } catch (Exception e) {
            log.error("Failed to generate HMAC SHA256 signature: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to generate payment signature");
        }
    }
}
