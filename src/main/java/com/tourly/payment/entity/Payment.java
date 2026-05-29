package com.tourly.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tourly.booking.entity.Booking;
import com.tourly.payment.enums.PaymentStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;


    private LocalDateTime createdAt;

    @Column(name = "razorpay_refund_id")
    private String razorpayRefundId;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_processed_at")
    private LocalDateTime refundProcessedAt;

    @Column(name = "refund_reason", length = 255)
    private String refundReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    private PaymentStage stage;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Column(name = "gateway_fee")
    private BigDecimal gatewayFee = BigDecimal.ZERO;

    private String currency = "INR";

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // getters and setters

    /**
     * @return Long return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Booking return the booking
     */
    public Booking getBooking() {
        return booking;
    }

    /**
     * @param booking the booking to set
     */
    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    /**
     * @return String return the razorpayOrderId
     */
    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    /**
     * @param razorpayOrderId the razorpayOrderId to set
     */
    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    /**
     * @return String return the razorpayPaymentId
     */
    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    /**
     * @param razorpayPaymentId the razorpayPaymentId to set
     */
    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    /**
     * @return BigDecimal return the amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * @return PaymentStatus return the status
     */
    public PaymentStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    /**
     * @return LocalDateTime return the createdAt
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

        public String getRazorpayRefundId() {
        return razorpayRefundId;
    }

    public void setRazorpayRefundId(String razorpayRefundId) {
        this.razorpayRefundId = razorpayRefundId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDateTime getRefundProcessedAt() {
        return refundProcessedAt;
    }

    public void setRefundProcessedAt(LocalDateTime refundProcessedAt) {
        this.refundProcessedAt = refundProcessedAt;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public PaymentStage getStage() {
        return stage;
    }

    public void setStage(PaymentStage stage) {
        this.stage = stage;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public BigDecimal getGatewayFee() {
        return gatewayFee;
    }

    public void setGatewayFee(BigDecimal gatewayFee) {
        this.gatewayFee = gatewayFee;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}