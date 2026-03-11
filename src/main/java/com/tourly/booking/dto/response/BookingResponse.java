package com.tourly.booking.dto.response;

import java.math.BigDecimal;

public class BookingResponse {

    private Long bookingId;

    private String tripTitle;

    private Integer seatsBooked;

    private BigDecimal totalPrice;

    private String bookingStatus;

    // Getters & Setters

    /**
     * @return Long return the bookingId
     */
    public Long getBookingId() {
        return bookingId;
    }

    /**
     * @param bookingId the bookingId to set
     */
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
     * @return String return the tripTitle
     */
    public String getTripTitle() {
        return tripTitle;
    }

    /**
     * @param tripTitle the tripTitle to set
     */
    public void setTripTitle(String tripTitle) {
        this.tripTitle = tripTitle;
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
     * @return String return the bookingStatus
     */
    public String getBookingStatus() {
        return bookingStatus;
    }

    /**
     * @param bookingStatus the bookingStatus to set
     */
    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

}