package com.tourly.payment.controller;

import com.tourly.common.dto.ApiResponse;
import com.tourly.payment.dto.request.AdminPayoutActionRequest;
import com.tourly.payment.dto.request.RequestPayoutRequest;
import com.tourly.payment.dto.response.AdminPayoutDetailResponse;
import com.tourly.payment.dto.response.BankAccountResponse;
import com.tourly.payment.dto.response.PayoutResponse;
import com.tourly.payment.enums.PayoutStatus;
import com.tourly.payment.service.PayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payouts")
@Tag(name = "Payouts", description = "Host payout request and admin management APIs")
public class PayoutController {

    private final PayoutService payoutService;

    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    // =========================================
    // HOST ENDPOINTS
    // =========================================

    @PostMapping("/request")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Request a payout for a booking", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PayoutResponse>> requestPayout(
            @Valid @RequestBody RequestPayoutRequest request) {
        PayoutResponse response = payoutService.requestPayout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payout requested successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Get my payout requests", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getMyPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PayoutResponse> payouts = payoutService.getMyPayouts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Payouts fetched", payouts));
    }

    @GetMapping("/trip-summary/{tripId}")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Get trip-level payout summary with 3-tranche breakdown", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<com.tourly.payment.dto.response.TripPayoutSummaryResponse>> getTripPayoutSummary(
            @PathVariable Long tripId) {
        com.tourly.payment.dto.response.TripPayoutSummaryResponse response = payoutService.getTripPayoutSummary(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip payout summary fetched", response));
    }

    @GetMapping("/bank-accounts")
    @PreAuthorize("hasAnyRole('HOST','PLANNER')")
    @Operation(summary = "Get my bank accounts", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getMyBankAccounts() {
        List<BankAccountResponse> accounts = payoutService.getMyBankAccounts();
        return ResponseEntity.ok(ApiResponse.success("Bank accounts fetched", accounts));
    }

    // =========================================
    // ADMIN ENDPOINTS
    // =========================================

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all payout requests (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getAllPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PayoutResponse> payouts = payoutService.getAllPayouts(pageable);
        return ResponseEntity.ok(ApiResponse.success("All payouts fetched", payouts));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payouts by status (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getPayoutsByStatus(
            @PathVariable PayoutStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PayoutResponse> payouts = payoutService.getPayoutsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Payouts by status fetched", payouts));
    }

    @PutMapping("/admin/{payoutId}/action")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve, reject, or release a payout (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PayoutResponse>> processPayoutAction(
            @PathVariable Long payoutId,
            @Valid @RequestBody AdminPayoutActionRequest request) {
        PayoutResponse response = payoutService.processPayoutAction(payoutId, request);
        return ResponseEntity.ok(ApiResponse.success("Payout " + request.getStatus().name().toLowerCase() + " successfully", response));
    }

    @GetMapping("/admin/{payoutId}/detail")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get enriched payout detail (Admin) - includes host history, trip stats, commission tier", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AdminPayoutDetailResponse>> getPayoutDetail(
            @PathVariable Long payoutId) {
        AdminPayoutDetailResponse response = payoutService.getPayoutDetail(payoutId);
        return ResponseEntity.ok(ApiResponse.success("Payout detail fetched", response));
    }
}
