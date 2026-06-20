package com.tourly.payment.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.common.exception.BadRequestException;
import com.tourly.payment.entity.Payment;
import com.tourly.payment.entity.PaymentStage;
import com.tourly.payment.enums.PaymentStageStatus;
import com.tourly.payment.enums.PaymentStatus;
import com.tourly.payment.repository.PaymentRepository;
import com.tourly.payment.repository.PaymentStageRepository;
import com.tourly.payment.service.PaymentWebhookService;
import com.tourly.notification.service.PaymentNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentStageRepository paymentStageRepository;
    private final BookingRepository bookingRepository;
    private final PaymentNotificationService paymentNotificationService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public PaymentWebhookServiceImpl(
            ObjectMapper objectMapper,
            PaymentRepository paymentRepository,
            PaymentStageRepository paymentStageRepository,
            BookingRepository bookingRepository,
            PaymentNotificationService paymentNotificationService
    ) {
        this.objectMapper = objectMapper;
        this.paymentRepository = paymentRepository;
        this.paymentStageRepository = paymentStageRepository;
        this.bookingRepository = bookingRepository;
        this.paymentNotificationService = paymentNotificationService;
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        verifyWebhookSignature(payload, signature);

        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();

            log.info("Received Razorpay webhook event: {}", event);

            switch (event) {
                case "payment.captured":
                    handlePaymentCaptured(root);
                    break;
                case "payment.failed":
                    handlePaymentFailed(root);
                    break;
                default:
                    log.info("Unhandled Razorpay webhook event ignored: {}", event);
            }

        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error processing Razorpay webhook", ex);
            throw new BadRequestException("Failed to process webhook payload");
        }
    }

    private void verifyWebhookSignature(String payload, String signature) {
        try {
            String generatedSignature = hmacSha256(payload, webhookSecret);

            if (!generatedSignature.equals(signature)) {
                log.warn("Invalid Razorpay webhook signature");
                throw new BadRequestException("Invalid webhook signature");
            }

        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Webhook signature verification failed", ex);
            throw new BadRequestException("Webhook signature verification failed");
        }
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    private void handlePaymentCaptured(JsonNode root) {
        String razorpayPaymentId = root.path("payload")
                .path("payment")
                .path("entity")
                .path("id")
                .asText();

        String razorpayOrderId = root.path("payload")
                .path("payment")
                .path("entity")
                .path("order_id")
                .asText();

        log.info("Processing payment.captured for orderId={}, paymentId={}", razorpayOrderId, razorpayPaymentId);

        var paymentOptional = paymentRepository.findByRazorpayOrderId(razorpayOrderId);

        if (paymentOptional.isEmpty()) {
            log.warn("Payment not found for razorpayOrderId={}", razorpayOrderId);
            return;
        }

        Payment payment = paymentOptional.get();

        // Idempotency: if already CAPTURED, ignore duplicate webhook
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Payment already marked CAPTURED for orderId={}, skipping duplicate webhook", razorpayOrderId);
            return;
        }

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(java.time.LocalDateTime.now());
        paymentRepository.save(payment);

        // Update payment stage
        PaymentStage stage = payment.getPaymentStage();
        if (stage != null && stage.getStatus() != PaymentStageStatus.PAID) {
            stage.setStatus(PaymentStageStatus.PAID);
            stage.setPaidAt(java.time.LocalDateTime.now());
            paymentStageRepository.save(stage);
            log.info("Payment stage {} marked PAID via webhook for bookingId={}",
                    stage.getStageNumber(), payment.getBooking() != null ? payment.getBooking().getId() : null);
        }

        // Update booking
        var booking = payment.getBooking();
        if (booking != null && stage != null) {
            java.math.BigDecimal newAmountPaid = booking.getAmountPaid().add(stage.getAmount());
            booking.setAmountPaid(newAmountPaid);
            booking.setAmountPending(booking.getTotalPrice().subtract(newAmountPaid));

            if (newAmountPaid.compareTo(booking.getTotalPrice()) >= 0) {
                booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.FULLY_PAID);
            } else {
                booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.PARTIALLY_PAID);
            }

            // First stage confirms booking
            if (stage.getStageNumber() == 1
                    && booking.getStatus() == com.tourly.booking.enums.BookingStatus.PENDING) {
                booking.setStatus(com.tourly.booking.enums.BookingStatus.CONFIRMED);
                booking.setConfirmedAt(java.time.LocalDateTime.now());
                booking.setExpiresAt(null);
            }

            bookingRepository.save(booking);
            log.info("Booking updated via webhook. bookingId={}, amountPaid={}, paymentStatus={}",
                    booking.getId(), newAmountPaid, booking.getPaymentStatus());
        }

        log.info("Payment marked CAPTURED via webhook for paymentId={}", payment.getId());
    }

    private void handlePaymentFailed(JsonNode root) {
        String razorpayPaymentId = root.path("payload")
                .path("payment")
                .path("entity")
                .path("id")
                .asText();

        String razorpayOrderId = root.path("payload")
                .path("payment")
                .path("entity")
                .path("order_id")
                .asText();

        log.info("Processing payment.failed for orderId={}, paymentId={}", razorpayOrderId, razorpayPaymentId);

        var paymentOptional = paymentRepository.findByRazorpayOrderId(razorpayOrderId);

        if (paymentOptional.isEmpty()) {
            log.warn("Payment not found for failed webhook, razorpayOrderId={}", razorpayOrderId);
            return;
        }

        Payment payment = paymentOptional.get();

        // If already CAPTURED, never overwrite it with FAILED
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Ignoring payment.failed because payment already CAPTURED for orderId={}", razorpayOrderId);
            return;
        }

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        var booking = payment.getBooking();
        if (booking != null) {
            booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.PENDING);
            bookingRepository.save(booking);

            // Notify traveler — Event 5: Payment Failed
            PaymentStage stage = payment.getPaymentStage();
            if (stage != null) {
                String tripTitle = booking.getTrip() != null ? booking.getTrip().getTitle() : "Trip";
                paymentNotificationService.notifyPaymentFailed(
                        booking.getTraveler().getId(), booking.getId(),
                        tripTitle, stage.getLabel(), stage.getAmount());
            }
        }

        log.info("Payment marked FAILED via webhook for paymentId={}", payment.getId());
    }
}

