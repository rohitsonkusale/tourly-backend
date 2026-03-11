package com.tourly.booking.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tourly.booking.dto.request.CreateBookingRequest;
import com.tourly.booking.dto.request.CancelBookingRequest;
import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // =====================================
    // BOOK TRIP (TRAVELER)
    // =====================================
    @PostMapping
    @PreAuthorize("hasRole('TRAVELER')")
    public BookingResponse bookTrip(
            @RequestBody CreateBookingRequest request) {

        return bookingService.bookTrip(request);
    }

    // =====================================
    // MY BOOKINGS (TRAVELER)
    // =====================================
    @GetMapping("/my")
    @PreAuthorize("hasRole('TRAVELER')")
    public List<BookingResponse> getMyBookings() {

        return bookingService.getMyBookings();
    }

    // =====================================
    // BOOKINGS FOR TRIP (PLANNER / HOST)
    // =====================================
    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public List<BookingResponse> getTripBookings(
            @PathVariable Long tripId) {

        return bookingService.getTripBookings(tripId);
    }

    // =====================================
    // CANCEL BOOKING
    // =====================================
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('TRAVELER')")
    public String cancelBooking(
            @PathVariable Long bookingId,
            @RequestBody CancelBookingRequest request) {

        bookingService.cancelBooking(bookingId, request);

        return "Booking cancelled successfully";
    }
}