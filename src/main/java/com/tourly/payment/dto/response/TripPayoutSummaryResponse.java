package com.tourly.payment.dto.response;

import com.tourly.payment.enums.PayoutTranche;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Trip-level payout summary showing the 3-tranche payout structure.
 * Used by the host dashboard to display payout timeline per trip.
 */
public class TripPayoutSummaryResponse {

    private Long tripId;
    private String tripTitle;
    private LocalDate departureDate;
    private LocalDate endDate;

    // Revenue summary
    private int totalConfirmedBookings;
    private BigDecimal totalRevenue;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal totalHostPayout;

    // The 3 tranches
    private TrancheDetail advance1;
    private TrancheDetail advance2;
    private TrancheDetail finalPayout;

    // Payout history (completed payouts for this trip)
    private List<PayoutHistoryItem> payoutHistory;

    // Whether there's any active (pending) request blocking new requests
    private boolean hasActiveRequest;

    // === Getters & Setters ===

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public String getTripTitle() { return tripTitle; }
    public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }

    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getTotalConfirmedBookings() { return totalConfirmedBookings; }
    public void setTotalConfirmedBookings(int totalConfirmedBookings) { this.totalConfirmedBookings = totalConfirmedBookings; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }

    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }

    public BigDecimal getTotalHostPayout() { return totalHostPayout; }
    public void setTotalHostPayout(BigDecimal totalHostPayout) { this.totalHostPayout = totalHostPayout; }

    public TrancheDetail getAdvance1() { return advance1; }
    public void setAdvance1(TrancheDetail advance1) { this.advance1 = advance1; }

    public TrancheDetail getAdvance2() { return advance2; }
    public void setAdvance2(TrancheDetail advance2) { this.advance2 = advance2; }

    public TrancheDetail getFinalPayout() { return finalPayout; }
    public void setFinalPayout(TrancheDetail finalPayout) { this.finalPayout = finalPayout; }

    public List<PayoutHistoryItem> getPayoutHistory() { return payoutHistory; }
    public void setPayoutHistory(List<PayoutHistoryItem> payoutHistory) { this.payoutHistory = payoutHistory; }

    public boolean isHasActiveRequest() { return hasActiveRequest; }
    public void setHasActiveRequest(boolean hasActiveRequest) { this.hasActiveRequest = hasActiveRequest; }

    // === Inner classes ===

    public static class TrancheDetail {
        private PayoutTranche tranche;
        private String label;
        private BigDecimal grossAmount;
        private BigDecimal commissionDeducted;
        private BigDecimal netAmount;
        private LocalDate releaseDate;
        private String status; // LOCKED, ELIGIBLE, REQUESTED, RELEASED, REJECTED
        private int bookingsCount;
        private List<BookingSummary> bookings;

        // If this tranche has a payout record
        private Long payoutId;
        private String utrNumber;
        private LocalDateTime releasedAt;
        private LocalDateTime requestedAt;
        private String adminMessage;

        public PayoutTranche getTranche() { return tranche; }
        public void setTranche(PayoutTranche tranche) { this.tranche = tranche; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public BigDecimal getGrossAmount() { return grossAmount; }
        public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

        public BigDecimal getCommissionDeducted() { return commissionDeducted; }
        public void setCommissionDeducted(BigDecimal commissionDeducted) { this.commissionDeducted = commissionDeducted; }

        public BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

        public LocalDate getReleaseDate() { return releaseDate; }
        public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getBookingsCount() { return bookingsCount; }
        public void setBookingsCount(int bookingsCount) { this.bookingsCount = bookingsCount; }

        public List<BookingSummary> getBookings() { return bookings; }
        public void setBookings(List<BookingSummary> bookings) { this.bookings = bookings; }

        public Long getPayoutId() { return payoutId; }
        public void setPayoutId(Long payoutId) { this.payoutId = payoutId; }

        public String getUtrNumber() { return utrNumber; }
        public void setUtrNumber(String utrNumber) { this.utrNumber = utrNumber; }

        public LocalDateTime getReleasedAt() { return releasedAt; }
        public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }

        public LocalDateTime getRequestedAt() { return requestedAt; }
        public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

        public String getAdminMessage() { return adminMessage; }
        public void setAdminMessage(String adminMessage) { this.adminMessage = adminMessage; }
    }

    public static class BookingSummary {
        private Long bookingId;
        private String bookingRef;
        private String travelerName;
        private BigDecimal amount;
        private LocalDateTime confirmedAt;

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public String getBookingRef() { return bookingRef; }
        public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }

        public String getTravelerName() { return travelerName; }
        public void setTravelerName(String travelerName) { this.travelerName = travelerName; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public LocalDateTime getConfirmedAt() { return confirmedAt; }
        public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    }

    public static class PayoutHistoryItem {
        private Long payoutId;
        private PayoutTranche tranche;
        private String trancheLabel;
        private BigDecimal grossAmount;
        private BigDecimal commissionDeducted;
        private BigDecimal netAmount;
        private LocalDateTime releasedAt;
        private String utrNumber;

        public Long getPayoutId() { return payoutId; }
        public void setPayoutId(Long payoutId) { this.payoutId = payoutId; }

        public PayoutTranche getTranche() { return tranche; }
        public void setTranche(PayoutTranche tranche) { this.tranche = tranche; }

        public String getTrancheLabel() { return trancheLabel; }
        public void setTrancheLabel(String trancheLabel) { this.trancheLabel = trancheLabel; }

        public BigDecimal getGrossAmount() { return grossAmount; }
        public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

        public BigDecimal getCommissionDeducted() { return commissionDeducted; }
        public void setCommissionDeducted(BigDecimal commissionDeducted) { this.commissionDeducted = commissionDeducted; }

        public BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

        public LocalDateTime getReleasedAt() { return releasedAt; }
        public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }

        public String getUtrNumber() { return utrNumber; }
        public void setUtrNumber(String utrNumber) { this.utrNumber = utrNumber; }
    }
}
