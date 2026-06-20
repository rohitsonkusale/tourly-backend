package com.tourly.payment.service;

import com.tourly.payment.dto.request.CreatePaymentRequest;
import com.tourly.payment.dto.request.VerifyPaymentRequest;
import com.tourly.payment.dto.response.PaymentResponse;
import com.tourly.payment.dto.response.UpcomingPaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    void verifyPayment(VerifyPaymentRequest request);

    List<UpcomingPaymentResponse> getUpcomingPayments();
}