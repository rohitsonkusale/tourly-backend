package com.tourly.booking.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tourly.trip.entity.Trip;
import com.tourly.auth.entity.User;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.enums.PaymentStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "traveler_id")
    private User traveler;

    private Integer seatsBooked;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "booking_ref", unique = true, length = 50)
    private String bookingRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private com.tourly.trip.entity.TripBatch batch;

    @Column(name = "base_amount")
    private BigDecimal baseAmount;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Getters and Setters

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
     * @return Trip return the trip
     */
    public Trip getTrip() {
        return trip;
    }

    /**
     * @param trip the trip to set
     */
    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    /**
     * @return User return the traveler
     */
    public User getTraveler() {
        return traveler;
    }

    /**
     * @param traveler the traveler to set
     */
    public void setTraveler(User traveler) {
        this.traveler = traveler;
    }

    /**
     * @return Integer return the seatsBooked
     */
    public Integer getSeatsBooked() {
        return seatsBooked;
    }

    /**
     * @param seatsBooked the seatsBooked to set
     */
    public void setSeatsBooked(Integer seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    /**
     * @return BigDecimal return the totalPrice
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * @param totalPrice the totalPrice to set
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * @return BookingStatus return the status
     */
    public BookingStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    /**
     * @return PaymentStatus return the paymentStatus
     */
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * @param paymentStatus the paymentStatus to set
     */
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
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

    /**
     * @return LocalDateTime return the updatedAt
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt the updatedAt to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    /**
     * @return LocalDateTime return the expiresAt
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * @param expiresAt the expiresAt to set
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getBookingRef() {
        return bookingRef;
    }

    public void setBookingRef(String bookingRef) {
        this.bookingRef = bookingRef;
    }

    public com.tourly.trip.entity.TripBatch getBatch() {
        return batch;
    }

    public void setBatch(com.tourly.trip.entity.TripBatch batch) {
        this.batch = batch;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}