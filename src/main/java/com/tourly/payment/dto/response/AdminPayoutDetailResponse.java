package com.tourly.payment.dto.response;

import com.tourly.payment.enums.PayoutStatus;
import com.tourly.payment.enums.PayoutTranche;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Enriched payout response for admin view.
 * Includes host payout history, trip booking stats, and commission tier info.
 */
public class AdminPayoutDetailResponse {

    // === Current Payout Info ===
    private Long id;
    private Long bookingId;
    private String bookingRef;
    private String tripTitle;
    private Long tripId;
    private BigDecimal grossAmount;
    private BigDecimal commissionDeducted;
    private BigDecimal commissionRate; // e.g. 0.12 or 0.15
    private BigDecimal tdsDeducted;
    private BigDecimal netAmount;
    private PayoutStatus status;
    private PayoutTranche tranche;
    private String adminMessage;
    private String utrNumber;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime releasedAt;

    // === Bank Info ===
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
    private String upiId;

    // === Host Info ===
    private Long hostId;
    private String hostName;
    private String hostEmail;
    private LocalDateTime hostRegisteredAt;
    private boolean isFoundingHost; // first 10 hosts - 12% commission for 3 months
    private String commissionTier; // "FOUNDING_12" or "STANDARD_15"

    // === Host Payout History ===
    private int totalPayoutsForHost;
    private BigDecimal totalReleasedAmountForHost;
    private boolean isFirstPayoutForHost;
    private PayoutHistoryEntry previousPayout; // last released payout for this host (if any)

    // === Trip Stats ===
    private int totalBookingsForTrip;
    private BigDecimal totalRevenueForTrip;
    private int totalPayoutsForTrip;
    private BigDecimal totalReleasedForTrip;

    // === Inner class for previous payout info ===
    public static class PayoutHistoryEntry {
        private Long payoutId;
        private String tripTitle;
        private String bookingRef;
        private BigDecimal netAmount;
        private LocalDateTime releasedAt;
        private String utrNumber;

        public Long getPayoutId() { return payoutId; }
        public void setPayoutId(Long payoutId) { this.payoutId = payoutId; }
        public String getTripTitle() { return tripTitle; }
        public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }
        public String getBookingRef() { return bookingRef; }
        public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
        public BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
        public LocalDateTime getReleasedAt() { return releasedAt; }
        public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
        public String getUtrNumber() { return utrNumber; }
        public void setUtrNumber(String utrNumber) { this.utrNumber = utrNumber; }
    }

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }

    public String getTripTitle() { return tripTitle; }
    public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

    public BigDecimal getCommissionDeducted() { return commissionDeducted; }
    public void setCommissionDeducted(BigDecimal commissionDeducted) { this.commissionDeducted = commissionDeducted; }

    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }

    public BigDecimal getTdsDeducted() { return tdsDeducted; }
    public void setTdsDeducted(BigDecimal tdsDeducted) { this.tdsDeducted = tdsDeducted; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }

    public PayoutTranche getTranche() { return tranche; }
    public void setTranche(PayoutTranche tranche) { this.tranche = tranche; }

    public String getAdminMessage() { return adminMessage; }
    public void setAdminMessage(String adminMessage) { this.adminMessage = adminMessage; }

    public String getUtrNumber() { return utrNumber; }
    public void setUtrNumber(String utrNumber) { this.utrNumber = utrNumber; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getHostEmail() { return hostEmail; }
    public void setHostEmail(String hostEmail) { this.hostEmail = hostEmail; }

    public LocalDateTime getHostRegisteredAt() { return hostRegisteredAt; }
    public void setHostRegisteredAt(LocalDateTime hostRegisteredAt) { this.hostRegisteredAt = hostRegisteredAt; }

    public boolean isFoundingHost() { return isFoundingHost; }
    public void setFoundingHost(boolean foundingHost) { isFoundingHost = foundingHost; }

    public String getCommissionTier() { return commissionTier; }
    public void setCommissionTier(String commissionTier) { this.commissionTier = commissionTier; }

    public int getTotalPayoutsForHost() { return totalPayoutsForHost; }
    public void setTotalPayoutsForHost(int totalPayoutsForHost) { this.totalPayoutsForHost = totalPayoutsForHost; }

    public BigDecimal getTotalReleasedAmountForHost() { return totalReleasedAmountForHost; }
    public void setTotalReleasedAmountForHost(BigDecimal totalReleasedAmountForHost) { this.totalReleasedAmountForHost = totalReleasedAmountForHost; }

    public boolean isFirstPayoutForHost() { return isFirstPayoutForHost; }
    public void setFirstPayoutForHost(boolean firstPayoutForHost) { isFirstPayoutForHost = firstPayoutForHost; }

    public PayoutHistoryEntry getPreviousPayout() { return previousPayout; }
    public void setPreviousPayout(PayoutHistoryEntry previousPayout) { this.previousPayout = previousPayout; }

    public int getTotalBookingsForTrip() { return totalBookingsForTrip; }
    public void setTotalBookingsForTrip(int totalBookingsForTrip) { this.totalBookingsForTrip = totalBookingsForTrip; }

    public BigDecimal getTotalRevenueForTrip() { return totalRevenueForTrip; }
    public void setTotalRevenueForTrip(BigDecimal totalRevenueForTrip) { this.totalRevenueForTrip = totalRevenueForTrip; }

    public int getTotalPayoutsForTrip() { return totalPayoutsForTrip; }
    public void setTotalPayoutsForTrip(int totalPayoutsForTrip) { this.totalPayoutsForTrip = totalPayoutsForTrip; }

    public BigDecimal getTotalReleasedForTrip() { return totalReleasedForTrip; }
    public void setTotalReleasedForTrip(BigDecimal totalReleasedForTrip) { this.totalReleasedForTrip = totalReleasedForTrip; }
}
