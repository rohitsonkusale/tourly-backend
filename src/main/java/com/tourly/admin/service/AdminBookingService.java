package com.tourly.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.enums.BookingStatus;

public interface AdminBookingService {

    Page<BookingResponse> getAllBookings(Pageable pageable);

    Page<BookingResponse> getBookingsByStatus(BookingStatus status, Pageable pageable);

    BookingResponse getBookingById(Long bookingId);
}