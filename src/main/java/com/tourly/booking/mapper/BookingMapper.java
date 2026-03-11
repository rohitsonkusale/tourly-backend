package com.tourly.booking.mapper;

import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.entity.Booking;

public class BookingMapper {

    public static BookingResponse toResponse(Booking booking) {

        BookingResponse response = new BookingResponse();

        response.setBookingId(booking.getId());

        if (booking.getTrip() != null) {
            response.setTripTitle(booking.getTrip().getTitle());
        }

        response.setSeatsBooked(booking.getSeatsBooked());

        response.setTotalPrice(booking.getTotalPrice());

        if (booking.getStatus() != null) {
            response.setBookingStatus(booking.getStatus().name());
        }

        return response;
    }
}