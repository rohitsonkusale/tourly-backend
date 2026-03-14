package com.tourly.payment.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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
import com.tourly.payment.dto.request.CreatePaymentRequest;
import com.tourly.payment.dto.request.VerifyPaymentRequest;
import com.tourly.payment.dto.response.PaymentResponse;
import com.tourly.payment.entity.Payment;
import com.tourly.payment.enums.PaymentStatus;
import com.tourly.payment.repository.PaymentRepository;
import com.tourly.payment.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {

        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // =========================================
    // HELPER: GET CURRENT USER
    // =========================================
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =========================================
    // HELPER: VALIDATE BOOKING OWNERSHIP
    // =========================================
    private void validateBookingOwnership(Booking booking, User currentUser) {
        if (booking.getTraveler() == null || booking.getTraveler().getId() == null) {
            throw new RuntimeException("Booking traveler not found");
        }

        if (!booking.getTraveler().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to pay for this booking");
        }
    }

    // =========================================
    // CREATE PAYMENT ORDER
    // =========================================
    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        try {
            User currentUser = getCurrentUser();

            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Ownership check
            validateBookingOwnership(booking, currentUser);

            // Booking state validations
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new RuntimeException("Cannot pay for a cancelled booking");
            }

            if (booking.getStatus() == BookingStatus.COMPLETED) {
                throw new RuntimeException("Cannot pay for a completed booking");
            }

            if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Booking payment window has expired");
            }

            if (booking.getPaymentStatus() == com.tourly.booking.enums.PaymentStatus.PAID) {
                throw new RuntimeException("Booking is already paid");
            }

            BigDecimal amount = booking.getTotalPrice();

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Invalid booking amount");
            }

            // Check existing payment first (important for OneToOne design)
            Payment existingPayment = paymentRepository
                    .findByBookingId(booking.getId())
                    .orElse(null);

            // If payment already exists
            if (existingPayment != null) {

                // Already paid
                if (existingPayment.getStatus() == PaymentStatus.SUCCESS) {
                    throw new RuntimeException("Booking is already paid");
                }

                // Pending payment already exists -> return same order (NO new Razorpay order)
                if (existingPayment.getStatus() == PaymentStatus.PENDING) {
                    PaymentResponse response = new PaymentResponse();
                    response.setRazorpayOrderId(existingPayment.getRazorpayOrderId());
                    response.setBookingId(booking.getId());
                    response.setStatus("ORDER_ALREADY_EXISTS");
                    return response;
                }
            }

            // Create Razorpay order only when really needed
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // paisa
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + booking.getId());

            Order order = razorpayClient.orders.create(orderRequest);

            Payment payment;

            // =========================================
            // CASE 1: Retry after FAILED payment
            // Reuse same payment row (important for OneToOne)
            // =========================================
            if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.FAILED) {
                payment = existingPayment;
                payment.setAmount(amount);
                payment.setRazorpayOrderId(order.get("id").toString());
                payment.setRazorpayPaymentId(null);
                payment.setStatus(PaymentStatus.PENDING);
                payment.setCreatedAt(LocalDateTime.now());

            } else {
                // =========================================
                // CASE 2: First payment creation
                // =========================================
                payment = new Payment();
                payment.setBooking(booking);
                payment.setAmount(amount);
                payment.setRazorpayOrderId(order.get("id").toString());
                payment.setRazorpayPaymentId(null);
                payment.setStatus(PaymentStatus.PENDING);
                payment.setCreatedAt(LocalDateTime.now());
            }

            paymentRepository.save(payment);

            PaymentResponse response = new PaymentResponse();
            response.setRazorpayOrderId(order.get("id").toString());
            response.setBookingId(booking.getId());
            response.setStatus("ORDER_CREATED");

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Payment creation failed: " + e.getMessage());
        }
    }

    // =========================================
    // VERIFY PAYMENT
    // =========================================
    @Override
    @Transactional
    public void verifyPayment(VerifyPaymentRequest request) {

        try {
            Payment payment = paymentRepository
                    .findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Idempotency protection
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                return;
            }

            Booking booking = payment.getBooking();

            if (booking == null) {
                throw new RuntimeException("Booking not found for payment");
            }

            // Prevent verifying cancelled/completed/expired bookings
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new RuntimeException("Booking is cancelled");
            }

            if (booking.getStatus() == BookingStatus.COMPLETED) {
                throw new RuntimeException("Booking is already completed");
            }

            if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Booking payment window has expired");
            }

            if (booking.getPaymentStatus() == com.tourly.booking.enums.PaymentStatus.PAID) {
                return;
            }

            String data =
                    request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

            String expectedSignature = hmacSHA256(data, razorpaySecret);

            if (!expectedSignature.equals(request.getRazorpaySignature())) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new RuntimeException("Invalid payment signature");
            }

            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setStatus(PaymentStatus.SUCCESS);

            booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.PAID);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);
            bookingRepository.save(booking);

        } catch (Exception e) {
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }

    // =========================================
    // HMAC SIGNATURE GENERATOR
    // =========================================
    private String hmacSHA256(String data, String key) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(), "HmacSHA256");

            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(data.getBytes());

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
            throw new RuntimeException("Failed to generate HMAC");
        }
    }
}