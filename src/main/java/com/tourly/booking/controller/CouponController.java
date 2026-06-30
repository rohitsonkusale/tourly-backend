package com.tourly.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.booking.dto.request.ValidateCouponRequest;
import com.tourly.booking.dto.response.CouponValidationResponse;
import com.tourly.booking.service.CouponService;
import com.tourly.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/coupons")
@Validated
@Tag(name = "Coupon", description = "Coupon validation for travelers at checkout")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('TRAVELER','ADMIN')")
    @Operation(summary = "Validate a coupon code",
               description = "Check if a coupon code is valid for the given trip and seat count. Returns discount details.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request) {
        CouponValidationResponse response = couponService.validateCoupon(request);
        String msg = response.isValid() ? "Coupon validated successfully" : "Coupon validation failed";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }
}
