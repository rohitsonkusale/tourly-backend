package com.tourly.booking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateBookingRequest {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Seats is required")
    @Min(value = 1, message = "Seats must be at least 1")
    @Max(value = 20, message = "Seats cannot exceed 20 in a single booking")
    private Integer seats;

    @Size(max = 50, message = "Coupon code cannot exceed 50 characters")
    private String couponCode;

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

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}