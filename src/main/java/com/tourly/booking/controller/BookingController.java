package com.tourly.booking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.booking.dto.request.CancelBookingRequest;
import com.tourly.booking.dto.request.CreateBookingRequest;
import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.service.BookingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/bookings")
@Validated
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
    public ResponseEntity<BookingResponse> bookTrip(
            @Valid @RequestBody CreateBookingRequest request) {

        BookingResponse response = bookingService.bookTrip(request);
        return ResponseEntity.ok(response);
    }

    // =====================================
    // MY BOOKINGS (TRAVELER)
    // =====================================
    @GetMapping("/my")
    @PreAuthorize("hasRole('TRAVELER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {

        List<BookingResponse> response = bookingService.getMyBookings();
        return ResponseEntity.ok(response);
    }

    // =====================================
    // BOOKINGS FOR TRIP (PLANNER / HOST)
    // =====================================
    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public ResponseEntity<List<BookingResponse>> getTripBookings(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId) {

        List<BookingResponse> response = bookingService.getTripBookings(tripId);
        return ResponseEntity.ok(response);
    }

    // =====================================
    // CANCEL BOOKING
    // =====================================
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('TRAVELER')")
    public ResponseEntity<String> cancelBooking(
            @PathVariable @Positive(message = "Booking ID must be greater than 0") Long bookingId,
            @Valid @RequestBody CancelBookingRequest request) {

        bookingService.cancelBooking(bookingId, request);
        return ResponseEntity.ok("Booking cancelled successfully");
    }
}