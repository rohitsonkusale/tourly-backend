package com.tourly.verification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.common.dto.ApiResponse;
import com.tourly.verification.dto.request.AdminVerificationActionRequest;
import com.tourly.verification.dto.response.PlannerVerificationResponse;
import com.tourly.verification.enums.VerificationStatus;
import com.tourly.verification.service.AdminPlannerVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/admin/verifications")
@Validated
@Tag(name = "Admin Planner Verification", description = "Admin APIs for reviewing, approving, rejecting, and suspending planner verification requests")
public class AdminPlannerVerificationController {

    private final AdminPlannerVerificationService adminPlannerVerificationService;

    public AdminPlannerVerificationController(AdminPlannerVerificationService adminPlannerVerificationService) {
        this.adminPlannerVerificationService = adminPlannerVerificationService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get pending verifications",
            description = "Fetch all planner verification requests with pending status",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<PlannerVerificationResponse>>> getPendingVerifications() {

        List<PlannerVerificationResponse> response = adminPlannerVerificationService.getPendingVerifications();
        return ResponseEntity.ok(ApiResponse.success("Pending verifications fetched successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get verifications by status",
            description = "Fetch all planner verification requests filtered by status",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<PlannerVerificationResponse>>> getVerificationsByStatus(
            @RequestParam VerificationStatus status) {

        List<PlannerVerificationResponse> response = adminPlannerVerificationService.getVerificationsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Verifications fetched successfully", response));
    }

    @GetMapping("/{verificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get verification by ID",
            description = "Fetch a specific planner verification request by ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<PlannerVerificationResponse>> getVerificationById(
            @PathVariable @Positive(message = "Verification ID must be greater than 0") Long verificationId) {

        PlannerVerificationResponse response = adminPlannerVerificationService.getVerificationById(verificationId);
        return ResponseEntity.ok(ApiResponse.success("Verification fetched successfully", response));
    }

    @PutMapping("/{verificationId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Approve verification",
            description = "Approve a planner verification request",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<PlannerVerificationResponse>> approveVerification(
            @PathVariable @Positive(message = "Verification ID must be greater than 0") Long verificationId) {

        PlannerVerificationResponse response = adminPlannerVerificationService.approveVerification(verificationId);
        return ResponseEntity.ok(ApiResponse.success("Verification approved successfully", response));
    }

    @PutMapping("/{verificationId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reject verification",
            description = "Reject a planner verification request with admin remarks",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<PlannerVerificationResponse>> rejectVerification(
            @PathVariable @Positive(message = "Verification ID must be greater than 0") Long verificationId,
            @Valid @RequestBody AdminVerificationActionRequest request) {

        PlannerVerificationResponse response = adminPlannerVerificationService.rejectVerification(verificationId, request);
        return ResponseEntity.ok(ApiResponse.success("Verification rejected successfully", response));
    }

    @PutMapping("/{verificationId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Suspend verification",
            description = "Suspend a planner verification request with admin remarks",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<PlannerVerificationResponse>> suspendVerification(
            @PathVariable @Positive(message = "Verification ID must be greater than 0") Long verificationId,
            @Valid @RequestBody AdminVerificationActionRequest request) {

        PlannerVerificationResponse response = adminPlannerVerificationService.suspendVerification(verificationId, request);
        return ResponseEntity.ok(ApiResponse.success("Verification suspended successfully", response));
    }
}