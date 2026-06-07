package com.tourly.payment.service.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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
import com.tourly.payment.entity.Payment;
import com.tourly.payment.enums.PaymentStatus;
import com.tourly.payment.repository.PaymentRepository;
import com.tourly.payment.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            RazorpayClient razorpayClient) {

        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.razorpayClient = razorpayClient;
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
    // CREATE PAYMENT ORDER
    // =========================================
    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Ownership check
        validateBookingOwnership(booking, currentUser);

        // Booking state validations
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.warn("Payment creation blocked: booking is cancelled. bookingId={}, userId={}",
                    booking.getId(), currentUser.getId());
            throw new BadRequestException("Cannot pay for a cancelled booking");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            log.warn("Payment creation blocked: booking is completed. bookingId={}, userId={}",
                    booking.getId(), currentUser.getId());
            throw new BadRequestException("Cannot pay for a completed booking");
        }

        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Payment creation blocked: booking expired. bookingId={}, expiresAt={}, userId={}",
                    booking.getId(), booking.getExpiresAt(), currentUser.getId());
            throw new BadRequestException("Booking payment window has expired");
        }

        if (booking.getPaymentStatus() == com.tourly.booking.enums.PaymentStatus.FULLY_PAID) {
            log.warn("Payment creation blocked: booking already paid. bookingId={}, userId={}",
                    booking.getId(), currentUser.getId());
            throw new BadRequestException("Booking is already paid");
        }

        BigDecimal amount = booking.getTotalPrice();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Payment creation blocked: invalid amount. bookingId={}, amount={}, userId={}",
                    booking.getId(), amount, currentUser.getId());
            throw new BadRequestException("Invalid booking amount");
        }

        // Check existing payment first
        Payment existingPayment = paymentRepository
                .findFirstByBookingIdOrderByCreatedAtDesc(booking.getId())
                .orElse(null);

        // If payment already exists
        if (existingPayment != null) {

            // Already paid
            if (existingPayment.getStatus() == PaymentStatus.PAID) {
                log.warn("Payment creation blocked: payment already successful. bookingId={}, paymentId={}",
                        booking.getId(), existingPayment.getId());
                throw new BadRequestException("Booking is already paid");
            }

            // Pending payment already exists -> return same order (NO new Razorpay order)
            if (existingPayment.getStatus() == PaymentStatus.CREATED) {
                log.info("Existing pending Razorpay order reused. bookingId={}, paymentId={}, razorpayOrderId={}",
                        booking.getId(), existingPayment.getId(), existingPayment.getRazorpayOrderId());

                PaymentResponse response = new PaymentResponse();
                response.setRazorpayOrderId(existingPayment.getRazorpayOrderId());
                response.setBookingId(booking.getId());
                response.setStatus("ORDER_ALREADY_EXISTS");
                return response;
            }
        }

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // paisa
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + booking.getId());

            Order order = razorpayClient.orders.create(orderRequest);

            Payment payment;

            // =========================================
            // CASE 1: Retry after FAILED payment
            // Create new payment with incremented attempt number
            // =========================================
            if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.FAILED) {
                payment = existingPayment;
                payment.setAmount(amount);
                payment.setRazorpayOrderId(order.get("id").toString());
                payment.setRazorpayPaymentId(null);
                payment.setStatus(PaymentStatus.CREATED);
                payment.setCreatedAt(LocalDateTime.now());
                payment.setAttemptNumber(existingPayment.getAttemptNumber() + 1);

                log.info("Retry payment order created for failed payment. bookingId={}, paymentId={}, razorpayOrderId={}",
                        booking.getId(), payment.getId(), order.get("id").toString());

            } else {
                // =========================================
                // CASE 2: First payment creation
                // =========================================
                payment = new Payment();
                payment.setBooking(booking);
                payment.setAmount(amount);
                payment.setRazorpayOrderId(order.get("id").toString());
                payment.setRazorpayPaymentId(null);
                payment.setStatus(PaymentStatus.CREATED);
                payment.setCreatedAt(LocalDateTime.now());

                log.info("New payment order created. bookingId={}, razorpayOrderId={}, amount={}",
                        booking.getId(), order.get("id").toString(), amount);
            }

            paymentRepository.save(payment);

            PaymentResponse response = new PaymentResponse();
            response.setRazorpayOrderId(order.get("id").toString());
            response.setBookingId(booking.getId());
            response.setStatus("ORDER_CREATED");

            return response;

        } catch (BadRequestException | ResourceNotFoundException | UnauthorizedActionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create Razorpay order for bookingId={}, userId={}, error={}",
                    booking.getId(), currentUser.getId(), e.getMessage(), e);
            throw new BadRequestException("Unable to create payment order");
        }
    }

    // =========================================
    // VERIFY PAYMENT
    // =========================================
    @Override
    @Transactional
    public void verifyPayment(VerifyPaymentRequest request) {

        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Idempotency protection
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Payment verification skipped (already successful). paymentId={}, bookingId={}, razorpayOrderId={}",
                    payment.getId(),
                    payment.getBooking() != null ? payment.getBooking().getId() : null,
                    request.getRazorpayOrderId());
            return;
        }

        Booking booking = payment.getBooking();

        if (booking == null) {
            log.error("Payment verification failed: booking missing for paymentId={}", payment.getId());
            throw new ResourceNotFoundException("Booking not found for payment");
        }

        // Prevent verifying cancelled/completed/expired bookings
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.warn("Payment verification blocked: booking cancelled. bookingId={}, paymentId={}",
                    booking.getId(), payment.getId());
            throw new BadRequestException("Booking is cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            log.warn("Payment verification blocked: booking already completed. bookingId={}, paymentId={}",
                    booking.getId(), payment.getId());
            throw new BadRequestException("Booking is already completed");
        }

        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Payment verification blocked: booking expired. bookingId={}, paymentId={}, expiresAt={}",
                    booking.getId(), payment.getId(), booking.getExpiresAt());
            throw new BadRequestException("Booking payment window has expired");
        }

        if (booking.getPaymentStatus() == com.tourly.booking.enums.PaymentStatus.FULLY_PAID) {
            log.info("Payment verification skipped: booking already marked paid. bookingId={}, paymentId={}",
                    booking.getId(), payment.getId());
            return;
        }

        String data = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        String expectedSignature = hmacSHA256(data, razorpaySecret);

        if (!expectedSignature.equals(request.getRazorpaySignature())) {
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.warn("Payment verification failed: invalid signature. bookingId={}, paymentId={}, razorpayOrderId={}, razorpayPaymentId={}",
                    booking.getId(),
                    payment.getId(),
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId());

            throw new BadRequestException("Invalid payment signature");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setStatus(PaymentStatus.PAID);

        booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.FULLY_PAID);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        log.info("Payment verified successfully. bookingId={}, paymentId={}, razorpayOrderId={}, razorpayPaymentId={}",
                booking.getId(),
                payment.getId(),
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId());
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
