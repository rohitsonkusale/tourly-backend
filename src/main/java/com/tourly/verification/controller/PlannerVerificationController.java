package com.tourly.verification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.common.dto.ApiResponse;
import com.tourly.verification.dto.request.PlannerVerificationRequest;
import com.tourly.verification.dto.response.PlannerVerificationResponse;
import com.tourly.verification.service.PlannerVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/verifications")
@Validated
@Tag(name = "Planner Verification", description = "APIs for planners/hosts to apply for verification and track verification status")
public class PlannerVerificationController {

    private final PlannerVerificationService plannerVerificationService;

    public PlannerVerificationController(PlannerVerificationService plannerVerificationService) {
        this.plannerVerificationService = plannerVerificationService;
    }

    @PostMapping("/apply")
    @Operation(
            summary = "Apply for planner verification",
            description = "Allows an authenticated user to apply for planner/host verification",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<PlannerVerificationResponse>> applyVerification(
            @Valid @RequestBody PlannerVerificationRequest request) {

        PlannerVerificationResponse response = plannerVerificationService.applyVerification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Verification request submitted successfully", response));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get my verification request",
            description = "Fetch the current logged-in user's verification request details",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<PlannerVerificationResponse>> getMyVerification() {

        PlannerVerificationResponse response = plannerVerificationService.getMyVerification();
        return ResponseEntity.ok(ApiResponse.success("Verification details fetched successfully", response));
    }
}