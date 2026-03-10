package com.tourly.trip.controller;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import com.tourly.trip.dto.request.CreateTripRequest;
import com.tourly.trip.dto.request.UpdateTripRequest;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.service.TripService;

@RestController
@RequestMapping("/api/trips")
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
    public TripResponse createTrip(@RequestBody CreateTripRequest request) {
        return tripService.createTrip(request);
    }

    // ========================================
    // GET ALL TRIPS (PUBLIC)
    // ========================================
    @GetMapping
    public Page<TripResponse> getAllTrips(Pageable pageable) {
        return tripService.getAllTrips(pageable);
    }

    // ========================================
    // GET TRIP BY ID
    // ========================================
    @GetMapping("/{tripId}")
    public TripResponse getTripById(@PathVariable Long tripId) {
        return tripService.getTripById(tripId);
    }

    // ========================================
    // SEARCH TRIPS
    // ========================================
    @GetMapping("/search")
    public Page<TripResponse> searchTrips(

            @RequestParam(required = false) String destination,

            @RequestParam(required = false) String host,

            @RequestParam(required = false) LocalDate startDate,

            @RequestParam(required = false) LocalDate endDate,

            Pageable pageable) {

        return tripService.searchTrips(destination, host, startDate, endDate, pageable);
    }

    // ========================================
    // UPDATE TRIPS
    // ========================================
    @PutMapping("/{tripId}")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public TripResponse updateTrip(
            @PathVariable Long tripId,
            @RequestBody UpdateTripRequest request) {

        return tripService.updateTrip(tripId, request);
    }

    // ========================================
    // DELETE TRIPS
    // ========================================
    @DeleteMapping("/{tripId}")
    @PreAuthorize("hasAnyRole('PLANNER','HOST')")
    public String deleteTrip(@PathVariable Long tripId) {

        tripService.deleteTrip(tripId);

        return "Trip deleted successfully";
    }

}