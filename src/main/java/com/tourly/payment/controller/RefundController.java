package com.tourly.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.common.dto.ApiResponse;
import com.tourly.common.exception.UnauthorizedActionException;
import com.tourly.payment.dto.request.RefundRequest;
import com.tourly.payment.dto.response.RefundResponse;
import com.tourly.payment.service.RefundService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@Validated
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/refund/{bookingId}")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @PathVariable Long bookingId,
            @Valid @RequestBody(required = false) RefundRequest request,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new UnauthorizedActionException("User is not authenticated");
        }

        String userEmail = authentication.getName();

        RefundResponse response = refundService.processFullRefund(bookingId, request, userEmail);

        return ResponseEntity.ok(
                ApiResponse.success("Refund processed successfully", response)
        );
    }
}