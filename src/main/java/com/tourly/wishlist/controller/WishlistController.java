package com.tourly.wishlist.controller;

import com.tourly.common.dto.ApiResponse;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.wishlist.service.WishlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Wishlist", description = "User wishlist/saved trips management")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{tripId}")
    @Operation(summary = "Add trip to wishlist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> addToWishlist(@PathVariable Long tripId) {
        wishlistService.addToWishlist(tripId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip added to wishlist"));
    }

    @DeleteMapping("/{tripId}")
    @Operation(summary = "Remove trip from wishlist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(@PathVariable Long tripId) {
        wishlistService.removeFromWishlist(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip removed from wishlist"));
    }

    @GetMapping
    @Operation(summary = "Get my wishlist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<TripResponse>>> getMyWishlist() {
        List<TripResponse> trips = wishlistService.getMyWishlist();
        return ResponseEntity.ok(ApiResponse.success("Wishlist fetched successfully", trips));
    }

    @GetMapping("/{tripId}/check")
    @Operation(summary = "Check if trip is in wishlist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkWishlist(@PathVariable Long tripId) {
        boolean inWishlist = wishlistService.isInWishlist(tripId);
        return ResponseEntity.ok(ApiResponse.success("Checked", Map.of("inWishlist", inWishlist)));
    }

    @GetMapping("/count")
    @Operation(summary = "Get wishlist count", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, Long>>> getWishlistCount() {
        long count = wishlistService.getWishlistCount();
        return ResponseEntity.ok(ApiResponse.success("Count fetched", Map.of("count", count)));
    }
}
