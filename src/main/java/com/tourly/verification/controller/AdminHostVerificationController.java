package com.tourly.verification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.common.dto.ApiResponse;
import com.tourly.verification.dto.request.AdminVerificationActionRequest;
import com.tourly.verification.dto.response.HostVerificationResponse;
import com.tourly.trip.enums.ApprovalStatus;
import com.tourly.verification.service.AdminHostVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/admin/host-verifications")
@Validated
@Tag(name = "Admin Host Verification", description = "Admin APIs for reviewing, approving, rejecting, and suspending host verification requests")
public class AdminHostVerificationController {

    private final AdminHostVerificationService adminHostVerificationService;

    public AdminHostVerificationController(AdminHostVerificationService adminHostVerificationService) {
        this.adminHostVerificationService = adminHostVerificationService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get pending verifications",
            description = "Fetch all host verification requests with pending status",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<HostVerificationResponse>>> getPendingVerifications() {
        List<HostVerificationResponse> response = adminHostVerificationService.getPendingVerifications();
        return ResponseEntity.ok(ApiResponse.success("Pending host verifications fetched successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get verifications by status",
            description = "Fetch all host verification requests filtered by status",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<HostVerificationResponse>>> getVerificationsByStatus(
            @RequestParam ApprovalStatus status) {
        List<HostVerificationResponse> response = adminHostVerificationService.getVerificationsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Host verifications fetched successfully", response));
    }

    @GetMapping("/{verificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get verification by ID",
            description = "Fetch a specific host verification request by ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<HostVerificationResponse>> getVerificationById(
            @PathVariable @Positive(message = "Verification ID must be greater than 0") Long verificationId) {
        HostVerificationResponse response = adminHostVerificationService.getVerificationById(verificationId);
        return ResponseEntity.ok(ApiResponse.success("Host verification fetched successfully", response));
    }

    @PutMapping("/{verificationId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Approve verification",
            description = "Approve a host verification request",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<HostVerificationResponse>> approveVerification(
            @PathVariable @Positive(message = "Verification ID must be greater than 0") Long verificationId) {
        HostVerificationResponse response = adminHostVerificationService.approveVerification(verificationId);
        return ResponseEntity.ok(ApiResponse.success("Host verification approved successfully", response));
    }

    @PutMapping("/{verificationId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reject verification",
            description = "Reject a host verification request with admin remarks",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<HostVerificationResponse>> rejectVerification(
            @PathVariable @Positive(message = "Verification ID must be greater than 0") Long verificationId,
            @Valid @RequestBody AdminVerificationActionRequest request) {
        HostVerificationResponse response = adminHostVerificationService.rejectVerification(verificationId, request);
        return ResponseEntity.ok(ApiResponse.success("Host verification rejected successfully", response));
    }

    @PutMapping("/{verificationId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Suspend verification",
            description = "Suspend a host verification request with admin remarks",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<HostVerificationResponse>> suspendVerification(
            @PathVariable @Positive(message = "Verification ID must be greater than 0") Long verificationId,
            @Valid @RequestBody AdminVerificationActionRequest request) {
        HostVerificationResponse response = adminHostVerificationService.suspendVerification(verificationId, request);
        return ResponseEntity.ok(ApiResponse.success("Host verification suspended successfully", response));
    }
}
