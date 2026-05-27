package com.tourly.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.admin.service.AdminBookingService;
import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/bookings")
@Validated
@Tag(name = "Admin Booking Management", description = "Admin APIs for monitoring and managing bookings")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    public AdminBookingController(AdminBookingService adminBookingService) {
        this.adminBookingService = adminBookingService;
    }

    // =========================================
    // GET ALL BOOKINGS
    // =========================================
    @GetMapping
    @Operation(
            summary = "Get all bookings",
            description = "Fetch paginated list of all bookings in the system",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBookings(
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Page must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Size must be greater than 0") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponse> response = adminBookingService.getAllBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched successfully", response));
    }

    // =========================================
    // GET BOOKINGS BY STATUS
    // =========================================
    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get bookings by status",
            description = "Fetch paginated bookings filtered by booking status",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBookingsByStatus(
            @PathVariable BookingStatus status,
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Page must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Size must be greater than 0") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponse> response = adminBookingService.getBookingsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Bookings by status fetched successfully", response));
    }

    // =========================================
    // GET BOOKING BY ID
    // =========================================
    @GetMapping("/{bookingId}")
    @Operation(
            summary = "Get booking by ID",
            description = "Fetch a specific booking by booking ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable @Positive(message = "Booking ID must be greater than 0") Long bookingId) {

        BookingResponse response = adminBookingService.getBookingById(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking fetched successfully", response));
    }
}