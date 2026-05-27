package com.tourly.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.common.dto.ApiResponse;
import com.tourly.payment.dto.request.CreatePaymentRequest;
import com.tourly.payment.dto.request.VerifyPaymentRequest;
import com.tourly.payment.dto.response.PaymentResponse;
import com.tourly.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@Validated
@PreAuthorize("hasRole('TRAVELER')")
@Tag(name = "Payment", description = "Payment APIs for creating Razorpay orders and verifying payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // =====================================
    // CREATE PAYMENT ORDER
    // =====================================
    @PostMapping("/create-order")
    @Operation(
            summary = "Create payment order",
            description = "Creates a Razorpay payment order for a valid pending booking belonging to the logged-in traveler"
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(request);

        return ResponseEntity.ok(ApiResponse.success("Payment order created successfully", response));
    }

    // =====================================
    // VERIFY PAYMENT
    // =====================================
    @PostMapping("/verify")
    @Operation(
            summary = "Verify payment",
            description = "Verifies Razorpay payment signature and confirms booking payment for the logged-in traveler"
    )
    public ResponseEntity<ApiResponse<Void>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {

        paymentService.verifyPayment(request);

        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully"));
    }
}