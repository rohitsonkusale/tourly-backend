package com.tourly.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.payment.dto.request.CreatePaymentRequest;
import com.tourly.payment.dto.request.VerifyPaymentRequest;
import com.tourly.payment.dto.response.PaymentResponse;
import com.tourly.payment.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // =====================================
    // CREATE PAYMENT ORDER (TRAVELER)
    // =====================================
    @PostMapping("/create-order")
    @PreAuthorize("hasRole('TRAVELER')")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }

    // =====================================
    // VERIFY PAYMENT (TRAVELER)
    // =====================================
    @PostMapping("/verify")
    @PreAuthorize("hasRole('TRAVELER')")
    public ResponseEntity<String> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {

        paymentService.verifyPayment(request);
        return ResponseEntity.ok("Payment verified successfully");
    }
}