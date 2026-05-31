package com.tourly.booking.service;

import java.util.List;

import com.tourly.booking.dto.request.CancelBookingRequest;
import com.tourly.booking.dto.request.CreateBookingRequest;
import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.dto.response.HostBookingResponse;

public interface BookingService {

    BookingResponse bookTrip(CreateBookingRequest request);

    List<BookingResponse> getMyBookings();

    List<BookingResponse> getTripBookings(Long tripId);

    void cancelBooking(Long bookingId, CancelBookingRequest request);

    List<HostBookingResponse> getMyTripBookings();
}