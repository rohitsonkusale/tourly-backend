package com.tourly.payment.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

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

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository) {

        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    // =========================================
    // CREATE PAYMENT ORDER
    // =========================================
    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        try {

            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            BigDecimal amount = booking.getTotalPrice();

            RazorpayClient razorpayClient =
                    new RazorpayClient(razorpayKey, razorpaySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100))); // paisa
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + booking.getId());

            Order order = razorpayClient.orders.create(orderRequest);

            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(amount);
            payment.setRazorpayOrderId(order.get("id"));
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCreatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            PaymentResponse response = new PaymentResponse();
            response.setRazorpayOrderId(order.get("id"));
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

            String data =
                    request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

            String expectedSignature = hmacSHA256(data, razorpaySecret);

            if (!expectedSignature.equals(request.getRazorpaySignature())) {
                throw new RuntimeException("Invalid payment signature");
            }

            Payment payment = paymentRepository
                    .findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setStatus(PaymentStatus.SUCCESS);

            Booking booking = payment.getBooking();

            // 👇 USE IT HERE
            booking.setPaymentStatus(
                    com.tourly.booking.enums.PaymentStatus.SUCCESS
            );

            booking.setStatus(
                    com.tourly.booking.enums.BookingStatus.CONFIRMED
            );

            paymentRepository.save(payment);

        } catch (Exception e) {
            throw new RuntimeException("Payment verification failed");
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
                if (hexByte.length() == 1)
                    hex.append('0');
                hex.append(hexByte);
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC");
        }
    }
}