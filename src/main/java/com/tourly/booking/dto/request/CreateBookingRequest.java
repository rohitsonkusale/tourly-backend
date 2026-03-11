package com.tourly.booking.dto.request;

public class CreateBookingRequest {

    private Long tripId;
    private Integer seats;

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }
}