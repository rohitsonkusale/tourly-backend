package com.tourly.payment.entity;

import com.tourly.auth.entity.User;
import com.tourly.booking.entity.Booking;
import com.tourly.trip.entity.Trip;
import com.tourly.payment.enums.PayeeType;
import com.tourly.payment.enums.PayoutStatus;
import com.tourly.payment.enums.PayoutTranche;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payout_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private User payee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payee_type", nullable = false, length = 50)
    private PayeeType payeeType;

    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "commission_deducted", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionDeducted;

    @Column(name = "tds_deducted", precision = 12, scale = 2)
    private BigDecimal tdsDeducted = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PayoutStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "tranche", length = 20)
    private PayoutTranche tranche;

    @Column(name = "razorpay_transfer_id", length = 255)
    private String razorpayTransferId;

    @Column(name = "utr_number", length = 255)
    private String utrNumber;

    @Column(name = "admin_message", columnDefinition = "TEXT")
    private String adminMessage;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    public Payout() {}

    @PrePersist
    protected void onCreate() { if (this.requestedAt == null) this.requestedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public User getPayee() { return payee; }
    public void setPayee(User payee) { this.payee = payee; }
    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }
    public PayeeType getPayeeType() { return payeeType; }
    public void setPayeeType(PayeeType payeeType) { this.payeeType = payeeType; }
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
    public String getRazorpayTransferId() { return razorpayTransferId; }
    public void setRazorpayTransferId(String razorpayTransferId) { this.razorpayTransferId = razorpayTransferId; }
    public String getUtrNumber() { return utrNumber; }
    public void setUtrNumber(String utrNumber) { this.utrNumber = utrNumber; }
    public String getAdminMessage() { return adminMessage; }
    public void setAdminMessage(String adminMessage) { this.adminMessage = adminMessage; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
}
