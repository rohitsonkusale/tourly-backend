package com.tourly.booking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingResponse {

    private Long bookingId;
    private String bookingRef;

    private String tripTitle;
    private Long tripId;
    private String tripCoverImage;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;
    private String destination;
    private String startsFrom;
    private Integer durationDays;
    private Integer durationNights;

    private String hostName;
    private String hostAvatar;
    private Long hostId;

    private Integer seatsBooked;
    private BigDecimal totalPrice;
    private BigDecimal amountPaid;
    private String bookingStatus;
    private String paymentStatus;

    private LocalDateTime createdAt;

    // Getters & Setters

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }

    public String getTripTitle() { return tripTitle; }
    public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public String getTripCoverImage() { return tripCoverImage; }
    public void setTripCoverImage(String tripCoverImage) { this.tripCoverImage = tripCoverImage; }

    public LocalDate getTripStartDate() { return tripStartDate; }
    public void setTripStartDate(LocalDate tripStartDate) { this.tripStartDate = tripStartDate; }

    public LocalDate getTripEndDate() { return tripEndDate; }
    public void setTripEndDate(LocalDate tripEndDate) { this.tripEndDate = tripEndDate; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartsFrom() { return startsFrom; }
    public void setStartsFrom(String startsFrom) { this.startsFrom = startsFrom; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public Integer getDurationNights() { return durationNights; }
    public void setDurationNights(Integer durationNights) { this.durationNights = durationNights; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getHostAvatar() { return hostAvatar; }
    public void setHostAvatar(String hostAvatar) { this.hostAvatar = hostAvatar; }

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }

    public Integer getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(Integer seatsBooked) { this.seatsBooked = seatsBooked; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}