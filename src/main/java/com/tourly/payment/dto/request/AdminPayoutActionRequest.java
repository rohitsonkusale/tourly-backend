package com.tourly.payment.dto.request;

import com.tourly.payment.enums.PayoutStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminPayoutActionRequest {

    @NotNull(message = "Status is required")
    private PayoutStatus status;

    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    private String utrNumber;

    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUtrNumber() { return utrNumber; }
    public void setUtrNumber(String utrNumber) { this.utrNumber = utrNumber; }
}
