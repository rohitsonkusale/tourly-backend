package com.tourly.payment.dto.response;

import com.tourly.payment.enums.PayoutStatus;
import com.tourly.payment.enums.PayoutTranche;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayoutResponse {

    private Long id;
    private Long bookingId;
    private String bookingRef;
    private String tripTitle;
    private Long tripId;
    private BigDecimal grossAmount;
    private BigDecimal commissionDeducted;
    private BigDecimal commissionRate;
    private BigDecimal tdsDeducted;
    private BigDecimal netAmount;
    private PayoutStatus status;
    private PayoutTranche tranche;
    private String adminMessage;
    private String utrNumber;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime releasedAt;

    // Bank info
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
    private String upiId;

    // Host info (for admin view)
    private String hostName;
    private String hostEmail;
    private Long hostId;

    // Getters & Setters
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

    public BigDecimal getTdsDeducted() { return tdsDeducted; }
    public void setTdsDeducted(BigDecimal tdsDeducted) { this.tdsDeducted = tdsDeducted; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }

    public PayoutTranche getTranche() { return tranche; }
    public void setTranche(PayoutTranche tranche) { this.tranche = tranche; }

    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }

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

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getHostEmail() { return hostEmail; }
    public void setHostEmail(String hostEmail) { this.hostEmail = hostEmail; }

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }
}
