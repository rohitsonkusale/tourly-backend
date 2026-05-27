package com.tourly.payment.dto.request;

import jakarta.validation.constraints.Size;

public class RefundRequest {

    @Size(max = 255, message = "Refund reason must not exceed 255 characters")
    private String reason;

    public RefundRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}