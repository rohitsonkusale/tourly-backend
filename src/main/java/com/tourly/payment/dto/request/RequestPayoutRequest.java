package com.tourly.payment.dto.request;

import com.tourly.payment.enums.PayoutTranche;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RequestPayoutRequest {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Tranche is required")
    private PayoutTranche tranche;

    // Kept for backward compatibility but not required
    private Long bookingId;

    private Long bankAccountId;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String note;

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public PayoutTranche getTranche() { return tranche; }
    public void setTranche(PayoutTranche tranche) { this.tranche = tranche; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getBankAccountId() { return bankAccountId; }
    public void setBankAccountId(Long bankAccountId) { this.bankAccountId = bankAccountId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
