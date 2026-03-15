package com.tourly.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.tourly.admin.service.AdminTripService;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.enums.TripStatus;

@RestController
@RequestMapping("/api/admin/trips")
public class AdminTripController {

    private final AdminTripService adminTripService;

    public AdminTripController(AdminTripService adminTripService) {
        this.adminTripService = adminTripService;
    }

    // =========================================
    // MONITORING
    // =========================================

    @GetMapping
    public Page<TripResponse> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return adminTripService.getAllTrips(pageable);
    }

    @GetMapping("/deleted")
    public Page<TripResponse> getDeletedTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return adminTripService.getDeletedTrips(pageable);
    }

    @GetMapping("/active")
    public Page<TripResponse> getActiveTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return adminTripService.getActiveTrips(pageable);
    }

    @GetMapping("/inactive")
    public Page<TripResponse> getInactiveTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return adminTripService.getInactiveTrips(pageable);
    }

    @GetMapping("/status/{status}")
    public Page<TripResponse> getTripsByStatus(
            @PathVariable TripStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return adminTripService.getTripsByStatus(status, pageable);
    }

    @GetMapping("/{tripId}")
    public TripResponse getTripById(@PathVariable Long tripId) {
        return adminTripService.getTripById(tripId);
    }

    // =========================================
    // MODERATION
    // =========================================

    @PutMapping("/{tripId}/deactivate")
    public TripResponse deactivateTrip(@PathVariable Long tripId) {
        return adminTripService.deactivateTrip(tripId);
    }

    @PutMapping("/{tripId}/reactivate")
    public TripResponse reactivateTrip(@PathVariable Long tripId) {
        return adminTripService.reactivateTrip(tripId);
    }

    @PutMapping("/{tripId}/dispute")
    public TripResponse markTripAsDisputed(@PathVariable Long tripId) {
        return adminTripService.markTripAsDisputed(tripId);
    }
}