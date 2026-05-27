package com.tourly.booking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.booking.dto.request.CancelBookingRequest;
import com.tourly.booking.dto.request.CreateBookingRequest;
import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.service.BookingService;
import com.tourly.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/bookings")
@Validated
@Tag(name = "Booking", description = "Booking management APIs for travelers, planners, hosts, and admins")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TRAVELER','ADMIN')")
    @Operation(
            summary = "Book a trip",
            description = "Allows a traveler or admin to create a booking for a trip",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<BookingResponse>> bookTrip(
            @Valid @RequestBody CreateBookingRequest request) {

        BookingResponse response = bookingService.bookTrip(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip booked successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('TRAVELER','ADMIN')")
    @Operation(
            summary = "Get my bookings",
            description = "Fetch all bookings for the currently logged-in traveler or admin",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {

        List<BookingResponse> response = bookingService.getMyBookings();

        return ResponseEntity.ok(ApiResponse.success("Bookings fetched successfully", response));
    }

    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('PLANNER','HOST','ADMIN')")
    @Operation(
            summary = "Get bookings for a trip",
            description = "Fetch all bookings for a specific trip owned by planner, host, or accessible by admin",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTripBookings(
            @PathVariable
            @Parameter(description = "Unique ID of the trip")
            @Positive(message = "Trip ID must be greater than 0")
            Long tripId) {

        List<BookingResponse> response = bookingService.getTripBookings(tripId);

        return ResponseEntity.ok(ApiResponse.success("Trip bookings fetched successfully", response));
    }

    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('TRAVELER','ADMIN')")
    @Operation(
            summary = "Cancel booking",
            description = "Allows a traveler or admin to cancel an existing booking",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable
            @Parameter(description = "Unique ID of the booking")
            @Positive(message = "Booking ID must be greater than 0")
            Long bookingId,

            @Valid @RequestBody CancelBookingRequest request) {

        bookingService.cancelBooking(bookingId, request);

        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully"));
    }
}