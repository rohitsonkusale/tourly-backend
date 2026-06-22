package com.tourly.trip.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.common.dto.ApiResponse;
import com.tourly.trip.dto.request.CreateTripRequest;
import com.tourly.trip.dto.request.UpdateTripRequest;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.dto.response.HostStatsResponse;
import com.tourly.trip.dto.response.HostAnalyticsResponse;
import com.tourly.trip.enums.TripStatus;
import com.tourly.trip.service.TripService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/trips")
@Validated
@Tag(name = "Trip", description = "Trip management APIs for public users, hosts, and admins")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    // ========================================
    // CREATE TRIP (Host / Admin)
    // ========================================
    @PostMapping
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(
            summary = "Create a new trip",
            description = "Allows a host or admin to create a new trip listing",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(
            @Valid @RequestBody CreateTripRequest request) {

        TripResponse response = tripService.createTrip(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip created successfully", response));
    }

    // ========================================
    // GET ALL TRIPS (PUBLIC)
    // ========================================
    @GetMapping
    @Operation(
            summary = "Get all public trips",
            description = "Fetches paginated list of all public and available trips"
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getAllTrips(
            @Parameter(hidden = true) Pageable pageable) {

        Page<TripResponse> response = tripService.getAllTrips(pageable);

        return ResponseEntity.ok(ApiResponse.success("Trips fetched successfully", response));
    }

    // ========================================
    // GET TRIP BY ID (PUBLIC)
    // ========================================
    @GetMapping("/{tripId}")
    @Operation(
            summary = "Get trip by ID",
            description = "Fetches details of a public trip by its ID"
    )
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId) {

        TripResponse response = tripService.getTripById(tripId);

        return ResponseEntity.ok(ApiResponse.success("Trip fetched successfully", response));
    }

    // ========================================
    // SEARCH TRIPS (PUBLIC)
    // ========================================
    @GetMapping("/search")
    @Operation(
            summary = "Search trips",
            description = "Searches public trips using destination, host, and date filters"
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> searchTrips(

            @RequestParam(required = false)
            @Size(max = 100, message = "Destination filter cannot exceed 100 characters")
            String destination,

            @RequestParam(required = false)
            @Size(max = 100, message = "Host filter cannot exceed 100 characters")
            String host,

            @RequestParam(required = false) LocalDate startDate,

            @RequestParam(required = false) LocalDate endDate,

            @Parameter(hidden = true) Pageable pageable) {

        Page<TripResponse> response =
                tripService.searchTrips(destination, host, startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponse.success("Trips fetched successfully", response));
    }

    // ========================================
    // ADVANCED SEARCH (PUBLIC — FULL FILTER SUPPORT)
    // ========================================
    @GetMapping("/search/filter")
    @Operation(
            summary = "Advanced trip search with full filters",
            description = "Searches public trips using destination, host, dates, price range, category, difficulty, trip type, departure city, and seat availability"
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> searchTripsAdvanced(

            @RequestParam(required = false)
            @Size(max = 100, message = "Destination filter cannot exceed 100 characters")
            String destination,

            @RequestParam(required = false)
            @Size(max = 100, message = "Host filter cannot exceed 100 characters")
            String host,

            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,

            @RequestParam(required = false) java.math.BigDecimal priceMin,
            @RequestParam(required = false) java.math.BigDecimal priceMax,

            @RequestParam(required = false)
            @Size(max = 50, message = "Category filter cannot exceed 50 characters")
            String category,

            @RequestParam(required = false)
            @Size(max = 50, message = "Difficulty filter cannot exceed 50 characters")
            String difficulty,

            @RequestParam(required = false)
            @Size(max = 100, message = "Trip type filter cannot exceed 100 characters")
            String tripType,

            @RequestParam(required = false)
            @Size(max = 100, message = "Starts from filter cannot exceed 100 characters")
            String startsFrom,

            @RequestParam(required = false, defaultValue = "false") boolean seatsAvailable,

            @Parameter(hidden = true) Pageable pageable) {

        Page<TripResponse> response = tripService.searchTripsAdvanced(
                destination, host, startDate, endDate,
                priceMin, priceMax, category, difficulty,
                tripType, startsFrom, seatsAvailable, pageable);

        return ResponseEntity.ok(ApiResponse.success("Trips fetched successfully", response));
    }

    // ========================================
    // MY TRIPS - ALL
    // ========================================
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(
            summary = "Get my trips",
            description = "Fetches all trips created by the currently logged-in host or admin",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getMyTrips(
            @Parameter(hidden = true) Pageable pageable) {

        Page<TripResponse> response = tripService.getMyTrips(pageable);

        return ResponseEntity.ok(ApiResponse.success("Your trips fetched successfully", response));
    }

    // ========================================
    // MY TRIPS - ACTIVE
    // ========================================
    @GetMapping("/my/active")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(
            summary = "Get my active trips",
            description = "Fetches active trips created by the currently logged-in host or admin",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getMyActiveTrips(
            @Parameter(hidden = true) Pageable pageable) {

        Page<TripResponse> response = tripService.getMyActiveTrips(pageable);

        return ResponseEntity.ok(ApiResponse.success("Your active trips fetched successfully", response));
    }

    // ========================================
    // MY TRIPS - INACTIVE
    // ========================================
    @GetMapping("/my/inactive")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(
            summary = "Get my inactive trips",
            description = "Fetches inactive trips created by the currently logged-in host or admin",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getMyInactiveTrips(
            @Parameter(hidden = true) Pageable pageable) {

        Page<TripResponse> response = tripService.getMyInactiveTrips(pageable);

        return ResponseEntity.ok(ApiResponse.success("Your inactive trips fetched successfully", response));
    }

    // ========================================
    // MY TRIPS - DELETED
    // ========================================
    @GetMapping("/my/deleted")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(
            summary = "Get my deleted trips",
            description = "Fetches soft-deleted trips created by the currently logged-in host or admin",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getMyDeletedTrips(
            @Parameter(hidden = true) Pageable pageable) {

        Page<TripResponse> response = tripService.getMyDeletedTrips(pageable);

        return ResponseEntity.ok(ApiResponse.success("Your deleted trips fetched successfully", response));
    }

    // ========================================
    // MY TRIPS - BY STATUS
    // ========================================
    @GetMapping("/my/status")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(
            summary = "Get my trips by status",
            description = "Fetches trips created by the logged-in host or admin filtered by status",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getMyTripsByStatus(
            @RequestParam TripStatus status,
            @Parameter(hidden = true) Pageable pageable) {

        Page<TripResponse> response = tripService.getMyTripsByStatus(status, pageable);

        return ResponseEntity.ok(ApiResponse.success("Filtered trips fetched successfully", response));
    }

    // ========================================
    // UPDATE TRIP
    // ========================================
    @PutMapping("/{tripId}")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(
            summary = "Update trip",
            description = "Allows a host or admin to update an existing trip",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId,
            @Valid @RequestBody UpdateTripRequest request) {

        TripResponse response = tripService.updateTrip(tripId, request);

        return ResponseEntity.ok(ApiResponse.success("Trip updated successfully", response));
    }

    // ========================================
    // DELETE TRIP
    // ========================================
    @DeleteMapping("/{tripId}")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(
            summary = "Delete trip",
            description = "Soft deletes a trip created by the logged-in host or admin",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
        @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId) {

        tripService.deleteTrip(tripId);

        return ResponseEntity.ok(ApiResponse.success("Trip deleted successfully"));
    }

    // ========================================
    // GET MY STATS (Host / Admin)
    // ========================================
    @GetMapping("/my/stats")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(summary = "Get my stats", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<HostStatsResponse>> getMyStats() {
        HostStatsResponse response = tripService.getHostStats();
        return ResponseEntity.ok(ApiResponse.success("Host stats calculated successfully", response));
    }

    @GetMapping("/my/analytics")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @Operation(summary = "Get my analytics", description = "Returns detailed analytics for the logged-in host",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<HostAnalyticsResponse>> getMyAnalytics() {
        HostAnalyticsResponse response = tripService.getHostAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Host analytics fetched successfully", response));
    }
}