package com.tourly.payment.service;

import com.tourly.payment.dto.request.CreatePaymentRequest;
import com.tourly.payment.dto.request.VerifyPaymentRequest;
import com.tourly.payment.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    void verifyPayment(VerifyPaymentRequest request);

}