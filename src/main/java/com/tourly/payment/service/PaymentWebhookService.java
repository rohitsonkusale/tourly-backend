package com.tourly.payment.service;

public interface PaymentWebhookService {

    void handleWebhook(String payload, String signature);
}