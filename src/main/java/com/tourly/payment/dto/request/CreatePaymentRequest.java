package com.tourly.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreatePaymentRequest {

    @NotNull(message = "Booking ID is required")
    @Positive(message = "Booking ID must be greater than 0")
    private Long bookingId;

    @NotNull(message = "Stage ID is required")
    @Positive(message = "Stage ID must be greater than 0")
    private Long stageId;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }
} 