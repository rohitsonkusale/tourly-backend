package com.tourly.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for a single upcoming payment stage shown on the traveler's dashboard.
 * Combines payment stage info with booking/trip context.
 */
public class UpcomingPaymentResponse {

    private Long stageId;
    private Long bookingId;
    private String bookingRef;
    private Integer stageNumber;
    private String label;
    private BigDecimal amount;
    private BigDecimal percentage;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime deadlineAt;
    private Boolean isImmediate;

    // Trip context
    private Long tripId;
    private String tripTitle;
    private String tripCoverImage;
    private LocalDate tripStartDate;
    private String destination;

    public UpcomingPaymentResponse() {}

    // ===== Getters & Setters =====

    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }

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

    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }

    public Boolean getIsImmediate() { return isImmediate; }
    public void setIsImmediate(Boolean isImmediate) { this.isImmediate = isImmediate; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public String getTripTitle() { return tripTitle; }
    public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }

    public String getTripCoverImage() { return tripCoverImage; }
    public void setTripCoverImage(String tripCoverImage) { this.tripCoverImage = tripCoverImage; }

    public LocalDate getTripStartDate() { return tripStartDate; }
    public void setTripStartDate(LocalDate tripStartDate) { this.tripStartDate = tripStartDate; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
}
