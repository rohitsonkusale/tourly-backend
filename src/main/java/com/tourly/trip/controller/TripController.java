package com.tourly.trip.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.trip.dto.request.CreateTripRequest;
import com.tourly.trip.dto.request.UpdateTripRequest;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.enums.TripStatus;
import com.tourly.trip.service.TripService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/trips")
@Validated
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    // ========================================
    // CREATE TRIP (Planner / Host Only)
    // ========================================
    @PostMapping
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody CreateTripRequest request) {
        TripResponse response = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================
    // GET ALL TRIPS (PUBLIC)
    // ========================================
    @GetMapping
    public ResponseEntity<Page<TripResponse>> getAllTrips(Pageable pageable) {
        Page<TripResponse> response = tripService.getAllTrips(pageable);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // GET TRIP BY ID
    // ========================================
    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripById(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId) {
        TripResponse response = tripService.getTripById(tripId);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // SEARCH TRIPS
    // ========================================
    @GetMapping("/search")
    public ResponseEntity<Page<TripResponse>> searchTrips(

            @RequestParam(required = false)
            @Size(max = 100, message = "Destination filter cannot exceed 100 characters")
            String destination,

            @RequestParam(required = false)
            @Size(max = 100, message = "Host filter cannot exceed 100 characters")
            String host,

            @RequestParam(required = false) LocalDate startDate,

            @RequestParam(required = false) LocalDate endDate,

            Pageable pageable) {

        Page<TripResponse> response = tripService.searchTrips(destination, host, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // MY TRIPS - ALL
    // ========================================
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public ResponseEntity<Page<TripResponse>> getMyTrips(Pageable pageable) {
        Page<TripResponse> response = tripService.getMyTrips(pageable);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // MY TRIPS - ACTIVE
    // ========================================
    @GetMapping("/my/active")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public ResponseEntity<Page<TripResponse>> getMyActiveTrips(Pageable pageable) {
        Page<TripResponse> response = tripService.getMyActiveTrips(pageable);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // MY TRIPS - INACTIVE
    // ========================================
    @GetMapping("/my/inactive")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public ResponseEntity<Page<TripResponse>> getMyInactiveTrips(Pageable pageable) {
        Page<TripResponse> response = tripService.getMyInactiveTrips(pageable);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // MY TRIPS - DELETED
    // ========================================
    @GetMapping("/my/deleted")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public ResponseEntity<Page<TripResponse>> getMyDeletedTrips(Pageable pageable) {
        Page<TripResponse> response = tripService.getMyDeletedTrips(pageable);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // MY TRIPS - BY STATUS
    // Example:
    // /api/trips/my/status?status=PUBLISHED
    // ========================================
    @GetMapping("/my/status")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public ResponseEntity<Page<TripResponse>> getMyTripsByStatus(
            @RequestParam TripStatus status,
            Pageable pageable) {

        Page<TripResponse> response = tripService.getMyTripsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // UPDATE TRIPS
    // ========================================
    @PutMapping("/{tripId}")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId,
            @Valid @RequestBody UpdateTripRequest request) {

        TripResponse response = tripService.updateTrip(tripId, request);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // DELETE TRIPS
    // ========================================
    @DeleteMapping("/{tripId}")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public ResponseEntity<String> deleteTrip(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId) {

        tripService.deleteTrip(tripId);
        return ResponseEntity.ok("Trip deleted successfully");
    }
}