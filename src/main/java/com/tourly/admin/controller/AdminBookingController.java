package com.tourly.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import com.tourly.admin.service.AdminBookingService;
import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.enums.BookingStatus;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    public AdminBookingController(AdminBookingService adminBookingService) {
        this.adminBookingService = adminBookingService;
    }

    // =========================================
    // GET ALL BOOKINGS
    // =========================================
    @GetMapping
    public Page<BookingResponse> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return adminBookingService.getAllBookings(pageable);
    }

    // =========================================
    // GET BOOKINGS BY STATUS
    // =========================================
    @GetMapping("/status/{status}")
    public Page<BookingResponse> getBookingsByStatus(
            @PathVariable BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return adminBookingService.getBookingsByStatus(status, pageable);
    }

    // =========================================
    // GET BOOKING BY ID
    // =========================================
    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(@PathVariable Long bookingId) {
        return adminBookingService.getBookingById(bookingId);
    }
}