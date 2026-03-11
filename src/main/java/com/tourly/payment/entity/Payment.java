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

}