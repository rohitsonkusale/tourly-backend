package com.tourly.payment.entity;

import com.tourly.auth.entity.User;
import com.tourly.booking.entity.Booking;
import com.tourly.payment.enums.PayeeType;
import com.tourly.payment.enums.PayoutStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
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

    @Column(name = "gross_amount", nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "commission_deducted", nullable = false)
    private BigDecimal commissionDeducted;

    @Column(name = "tds_deducted")
    private BigDecimal tdsDeducted = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PayoutStatus status;

    @Column(name = "razorpay_transfer_id")
    private String razorpayTransferId;

    @Column(name = "utr_number")
    private String utrNumber;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    public Payout() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public User getPayee() {
        return payee;
    }

    public void setPayee(User payee) {
        this.payee = payee;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public PayeeType getPayeeType() {
        return payeeType;
    }

    public void setPayeeType(PayeeType payeeType) {
        this.payeeType = payeeType;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCommissionDeducted() {
        return commissionDeducted;
    }

    public void setCommissionDeducted(BigDecimal commissionDeducted) {
        this.commissionDeducted = commissionDeducted;
    }

    public BigDecimal getTdsDeducted() {
        return tdsDeducted;
    }

    public void setTdsDeducted(BigDecimal tdsDeducted) {
        this.tdsDeducted = tdsDeducted;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutStatus status) {
        this.status = status;
    }

    public String getRazorpayTransferId() {
        return razorpayTransferId;
    }

    public void setRazorpayTransferId(String razorpayTransferId) {
        this.razorpayTransferId = razorpayTransferId;
    }

    public String getUtrNumber() {
        return utrNumber;
    }

    public void setUtrNumber(String utrNumber) {
        this.utrNumber = utrNumber;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }
}
