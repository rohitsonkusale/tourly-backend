package com.tourly.verification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.common.dto.ApiResponse;
import com.tourly.verification.dto.request.HostVerificationRequest;
import com.tourly.verification.dto.response.HostVerificationResponse;
import com.tourly.verification.service.HostVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/host-verifications")
@Validated
@PreAuthorize("hasAnyRole('HOST','ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Host Verification", description = "APIs for hosts to submit and track their KYC verification")
public class HostVerificationController {

    private final HostVerificationService hostVerificationService;

    public HostVerificationController(HostVerificationService hostVerificationService) {
        this.hostVerificationService = hostVerificationService;
    }

    @PostMapping("/apply")
    @Operation(
            summary = "Submit host KYC verification",
            description = "Allows a HOST to submit or resubmit their KYC documents for admin review"
    )
    public ResponseEntity<ApiResponse<HostVerificationResponse>> applyVerification(
            @Valid @RequestBody HostVerificationRequest request) {

        HostVerificationResponse response = hostVerificationService.applyVerification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Host verification submitted successfully", response));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get my host KYC status",
            description = "Returns the current KYC verification status for the logged-in host"
    )
    public ResponseEntity<ApiResponse<HostVerificationResponse>> getMyVerification() {

        HostVerificationResponse response = hostVerificationService.getMyVerification();
        return ResponseEntity.ok(ApiResponse.success("Host verification status fetched successfully", response));
    }
}
