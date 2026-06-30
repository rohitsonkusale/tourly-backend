package com.tourly.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.admin.dto.request.CreateCouponRequest;
import com.tourly.admin.dto.response.AdminCouponResponse;
import com.tourly.admin.service.AdminCouponService;
import com.tourly.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/api/admin/coupons")
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Coupon Management", description = "Admin CRUD for coupon/discount codes")
@SecurityRequirement(name = "bearerAuth")
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    public AdminCouponController(AdminCouponService adminCouponService) {
        this.adminCouponService = adminCouponService;
    }

    @PostMapping
    @Operation(summary = "Create a new coupon")
    public ResponseEntity<ApiResponse<AdminCouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request) {
        AdminCouponResponse response = adminCouponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all coupons (paginated)")
    public ResponseEntity<ApiResponse<Page<AdminCouponResponse>>> getAllCoupons(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminCouponResponse> response = adminCouponService.getAllCoupons(pageable);
        return ResponseEntity.ok(ApiResponse.success("Coupons fetched successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coupon by ID")
    public ResponseEntity<ApiResponse<AdminCouponResponse>> getCouponById(
            @PathVariable @Positive Long id) {
        AdminCouponResponse response = adminCouponService.getCouponById(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon fetched successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update coupon status (ACTIVE, PAUSED, EXPIRED)")
    public ResponseEntity<ApiResponse<AdminCouponResponse>> updateCouponStatus(
            @PathVariable @Positive Long id,
            @RequestParam String status) {
        AdminCouponResponse response = adminCouponService.updateCouponStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Coupon status updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a coupon (only if unused)")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(
            @PathVariable @Positive Long id) {
        adminCouponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted successfully"));
    }
}
