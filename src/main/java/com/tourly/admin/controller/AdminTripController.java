package com.tourly.admin.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.admin.dto.request.TripModerationRequest;
import com.tourly.admin.service.AdminTripService;
import com.tourly.common.dto.ApiResponse;
import com.tourly.trip.dto.response.TripEditLogResponse;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.enums.TripStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/api/admin/trips")
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Trip Management", description = "Admin APIs for monitoring and moderating all trips")
@SecurityRequirement(name = "bearerAuth")
public class AdminTripController {

    private final AdminTripService adminTripService;

    public AdminTripController(AdminTripService adminTripService) {
        this.adminTripService = adminTripService;
    }

    private Pageable buildPageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    // =========================================
    // MONITORING
    // =========================================

    @GetMapping
    @Operation(
            summary = "Get all trips",
            description = "Fetch all trips in the system for admin monitoring"
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getAllTrips(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-based)")
            @PositiveOrZero(message = "Page must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of records per page")
            @Positive(message = "Size must be greater than 0")
            int size
    ) {
        Pageable pageable = buildPageable(page, size);
        Page<TripResponse> response = adminTripService.getAllTrips(pageable);

        return ResponseEntity.ok(ApiResponse.success("All trips fetched successfully", response));
    }

    @GetMapping("/deleted")
    @Operation(
            summary = "Get deleted trips",
            description = "Fetch all soft-deleted trips for admin monitoring"
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getDeletedTrips(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-based)")
            @PositiveOrZero(message = "Page must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of records per page")
            @Positive(message = "Size must be greater than 0")
            int size
    ) {
        Pageable pageable = buildPageable(page, size);
        Page<TripResponse> response = adminTripService.getDeletedTrips(pageable);

        return ResponseEntity.ok(ApiResponse.success("Deleted trips fetched successfully", response));
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get active trips",
            description = "Fetch all active trips for admin monitoring"
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getActiveTrips(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-based)")
            @PositiveOrZero(message = "Page must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of records per page")
            @Positive(message = "Size must be greater than 0")
            int size
    ) {
        Pageable pageable = buildPageable(page, size);
        Page<TripResponse> response = adminTripService.getActiveTrips(pageable);

        return ResponseEntity.ok(ApiResponse.success("Active trips fetched successfully", response));
    }

    @GetMapping("/inactive")
    @Operation(
            summary = "Get inactive trips",
            description = "Fetch all inactive trips for admin monitoring"
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getInactiveTrips(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-based)")
            @PositiveOrZero(message = "Page must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of records per page")
            @Positive(message = "Size must be greater than 0")
            int size
    ) {
        Pageable pageable = buildPageable(page, size);
        Page<TripResponse> response = adminTripService.getInactiveTrips(pageable);

        return ResponseEntity.ok(ApiResponse.success("Inactive trips fetched successfully", response));
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get trips by status",
            description = "Fetch all trips filtered by trip status for admin monitoring"
    )
    public ResponseEntity<ApiResponse<Page<TripResponse>>> getTripsByStatus(
            @PathVariable
            @Parameter(description = "Trip status filter")
            TripStatus status,

            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-based)")
            @PositiveOrZero(message = "Page must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of records per page")
            @Positive(message = "Size must be greater than 0")
            int size
    ) {
        Pageable pageable = buildPageable(page, size);
        Page<TripResponse> response = adminTripService.getTripsByStatus(status, pageable);

        return ResponseEntity.ok(ApiResponse.success("Trips by status fetched successfully", response));
    }

    @GetMapping("/{tripId}")
    @Operation(
            summary = "Get trip by ID",
            description = "Fetch a specific trip by ID for admin review"
    )
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(
            @PathVariable
            @Parameter(description = "Unique ID of the trip")
            @Positive(message = "Trip ID must be greater than 0")
            Long tripId) {

        TripResponse response = adminTripService.getTripById(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip fetched successfully", response));
    }

    // =========================================
    // MODERATION
    // =========================================

    @PutMapping("/{tripId}/deactivate")
    @Operation(
            summary = "Deactivate trip",
            description = "Deactivate a trip for moderation purposes"
    )
    public ResponseEntity<ApiResponse<TripResponse>> deactivateTrip(
            @PathVariable
            @Parameter(description = "Unique ID of the trip")
            @Positive(message = "Trip ID must be greater than 0")
            Long tripId) {

        TripResponse response = adminTripService.deactivateTrip(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip deactivated successfully", response));
    }

    @PutMapping("/{tripId}/reactivate")
    @Operation(
            summary = "Reactivate trip",
            description = "Reactivate a previously inactive trip"
    )
    public ResponseEntity<ApiResponse<TripResponse>> reactivateTrip(
            @PathVariable
            @Parameter(description = "Unique ID of the trip")
            @Positive(message = "Trip ID must be greater than 0")
            Long tripId) {

        TripResponse response = adminTripService.reactivateTrip(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip reactivated successfully", response));
    }

    @PutMapping("/{tripId}/dispute")
    @Operation(summary = "Mark trip as disputed", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripResponse>> markTripAsDisputed(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId) {
        TripResponse response = adminTripService.markTripAsDisputed(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip marked as disputed successfully", response));
    }

    // =========================================
    // APPROVAL QUEUE
    // =========================================

    @GetMapping("/pending-approval")
    @Operation(
            summary = "Get trips pending admin approval",
            description = "Returns all trips submitted by hosts with approvalStatus = PENDING",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<TripResponse>>> getPendingApprovalTrips() {
        List<TripResponse> response = adminTripService.getPendingApprovalTrips();
        return ResponseEntity.ok(ApiResponse.success("Pending approval trips fetched successfully", response));
    }

    @PutMapping("/{tripId}/approve")
    @Operation(
            summary = "Approve trip",
            description = "Approves a trip — sets approvalStatus=APPROVED, status=PUBLISHED, active=true",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<TripResponse>> approveTrip(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId) {
        TripResponse response = adminTripService.approveTrip(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip approved successfully", response));
    }

    @PutMapping("/{tripId}/reject")
    @Operation(
            summary = "Reject trip",
            description = "Rejects a trip with a mandatory admin message. Sets approvalStatus=REJECTED.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<TripResponse>> rejectTrip(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId,
            @Valid @RequestBody TripModerationRequest request) {
        TripResponse response = adminTripService.rejectTrip(tripId, request.getAdminMessage());
        return ResponseEntity.ok(ApiResponse.success("Trip rejected successfully", response));
    }

    @PutMapping("/{tripId}/pending-review")
    @Operation(
            summary = "Mark trip as pending review",
            description = "Requests changes from host with a mandatory message. Sets approvalStatus=PENDING_REVIEW.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<TripResponse>> markTripPendingReview(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId,
            @Valid @RequestBody TripModerationRequest request) {
        TripResponse response = adminTripService.markTripPendingReview(tripId, request.getAdminMessage());
        return ResponseEntity.ok(ApiResponse.success("Trip marked as pending review", response));
    }

    // =========================================
    // EDIT HISTORY
    // =========================================

    @GetMapping("/{tripId}/edit-history")
    @Operation(
            summary = "Get trip edit history",
            description = "Returns a detailed log of all edits made to a trip, grouped by edit session. Shows old vs new values, who edited, and the admin message that prompted the edit.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<TripEditLogResponse>> getTripEditHistory(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId) {
        TripEditLogResponse response = adminTripService.getTripEditHistory(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip edit history fetched successfully", response));
    }
}