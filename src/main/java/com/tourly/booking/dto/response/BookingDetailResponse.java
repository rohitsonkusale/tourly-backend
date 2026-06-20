package com.tourly.booking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Rich booking detail response for the Traveler Booking Details page.
 * Includes trip info, host info, payment stages, and financial summary.
 */
public class BookingDetailResponse {

    // Booking Info
    private Long bookingId;
    private String bookingRef;
    private String bookingStatus;
    private String paymentStatus;
    private String scheduleType;
    private Integer seatsBooked;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;

    // Financial Summary
    private BigDecimal baseAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
    private BigDecimal amountPaid;
    private BigDecimal amountPending;

    // Trip Info
    private Long tripId;
    private String tripTitle;
    private String tripDescription;
    private String tripCoverImage;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;
    private Integer durationDays;
    private Integer durationNights;
    private String startsFrom;
    private String endsAt;
    private String destination;
    private String cancellationPolicy;

    // Host Info
    private Long hostId;
    private String hostName;
    private String hostAvatar;

    // Payment Stages
    private List<PaymentStageInfo> paymentStages;

    public BookingDetailResponse() {}

    // ===== Inner class for payment stages =====
    public static class PaymentStageInfo {
        private Long stageId;
        private Integer stageNumber;
        private String label;
        private BigDecimal amount;
        private BigDecimal percentage;
        private String status;
        private LocalDate dueDate;
        private LocalDate invoiceOpenDate;
        private LocalDateTime deadlineAt;
        private LocalDateTime invoiceSentAt;
        private Boolean isImmediate;
        private LocalDateTime paidAt;

        public PaymentStageInfo() {}

        public Long getStageId() { return stageId; }
        public void setStageId(Long stageId) { this.stageId = stageId; }
        public Integer getStageNumber() { return stageNumber; }
        public void setStageNumber(Integer stageNumber) { this.stageNumber = stageNumber; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public LocalDate getInvoiceOpenDate() { return invoiceOpenDate; }
        public void setInvoiceOpenDate(LocalDate invoiceOpenDate) { this.invoiceOpenDate = invoiceOpenDate; }
        public LocalDateTime getDeadlineAt() { return deadlineAt; }
        public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }
        public LocalDateTime getInvoiceSentAt() { return invoiceSentAt; }
        public void setInvoiceSentAt(LocalDateTime invoiceSentAt) { this.invoiceSentAt = invoiceSentAt; }
        public Boolean getIsImmediate() { return isImmediate; }
        public void setIsImmediate(Boolean isImmediate) { this.isImmediate = isImmediate; }
        public LocalDateTime getPaidAt() { return paidAt; }
        public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    }

    // ===== Getters & Setters =====

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }

    public Integer getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(Integer seatsBooked) { this.seatsBooked = seatsBooked; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getAmountPending() { return amountPending; }
    public void setAmountPending(BigDecimal amountPending) { this.amountPending = amountPending; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public String getTripTitle() { return tripTitle; }
    public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }

    public String getTripDescription() { return tripDescription; }
    public void setTripDescription(String tripDescription) { this.tripDescription = tripDescription; }

    public String getTripCoverImage() { return tripCoverImage; }
    public void setTripCoverImage(String tripCoverImage) { this.tripCoverImage = tripCoverImage; }

    public LocalDate getTripStartDate() { return tripStartDate; }
    public void setTripStartDate(LocalDate tripStartDate) { this.tripStartDate = tripStartDate; }

    public LocalDate getTripEndDate() { return tripEndDate; }
    public void setTripEndDate(LocalDate tripEndDate) { this.tripEndDate = tripEndDate; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public Integer getDurationNights() { return durationNights; }
    public void setDurationNights(Integer durationNights) { this.durationNights = durationNights; }

    public String getStartsFrom() { return startsFrom; }
    public void setStartsFrom(String startsFrom) { this.startsFrom = startsFrom; }

    public String getEndsAt() { return endsAt; }
    public void setEndsAt(String endsAt) { this.endsAt = endsAt; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(String cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getHostAvatar() { return hostAvatar; }
    public void setHostAvatar(String hostAvatar) { this.hostAvatar = hostAvatar; }

    public List<PaymentStageInfo> getPaymentStages() { return paymentStages; }
    public void setPaymentStages(List<PaymentStageInfo> paymentStages) { this.paymentStages = paymentStages; }
}
