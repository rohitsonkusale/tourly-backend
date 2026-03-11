package com.tourly.payment.controller;

import org.springframework.web.bind.annotation.*;

import com.tourly.payment.dto.request.CreatePaymentRequest;
import com.tourly.payment.dto.response.PaymentResponse;
import com.tourly.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public PaymentResponse createPayment(
            @RequestBody CreatePaymentRequest request) {

        return paymentService.createPayment(request);
    }
}