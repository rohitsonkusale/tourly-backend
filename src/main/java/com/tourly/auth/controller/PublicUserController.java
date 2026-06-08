package com.tourly.auth.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tourly.auth.dto.response.PublicHostResponse;
import com.tourly.auth.dto.response.PublicHostResponse.HostedTripSummary;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.common.dto.ApiResponse;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.enums.TripStatus;
import com.tourly.trip.repository.TripRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;

import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Public User", description = "Public APIs for viewing host profiles")
public class PublicUserController {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;

    public PublicUserController(UserRepository userRepository,
                                TripRepository tripRepository,
                                BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/{userId}/public-profile")
    @Operation(
            summary = "Get public host profile",
            description = "Returns publicly visible host profile including stats and published trips. No auth required."
    )
    public ResponseEntity<ApiResponse<PublicHostResponse>> getPublicHostProfile(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long userId) {

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Host not found with ID: " + userId));

        // Fetch all trips by this host
        List<Trip> allTrips = tripRepository
                .findByHostIdOrPlannerId(userId, Pageable.unpaged())
                .getContent();

        long totalTrips = allTrips.size();
        long publishedTrips = allTrips.stream()
                .filter(t -> TripStatus.PUBLISHED.equals(t.getStatus()))
                .count();

        long totalSeatsOffered = allTrips.stream()
                .mapToLong(t -> t.getTotalSeats() != null ? t.getTotalSeats() : 0).sum();
        long totalSeatsBooked = allTrips.stream()
                .mapToLong(t -> t.getBookedSeats() != null ? t.getBookedSeats() : 0).sum();

        double occupancyRate = totalSeatsOffered > 0
                ? Math.round((totalSeatsBooked * 100.0 / totalSeatsOffered) * 10.0) / 10.0
                : 0.0;

        long totalBookings = bookingRepository.countBookingsByHostId(userId);
        java.math.BigDecimal totalRevenue = bookingRepository.sumEarningsByHostId(userId);

        // Published trips only for public view
        List<HostedTripSummary> hostedTripSummaries = allTrips.stream()
                .filter(t -> TripStatus.PUBLISHED.equals(t.getStatus())
                        && Boolean.TRUE.equals(t.getActive())
                        && !Boolean.TRUE.equals(t.getDeleted()))
                .map(t -> {
                    HostedTripSummary s = new HostedTripSummary();
                    s.setId(t.getId());
                    s.setTitle(t.getTitle());
                    if (t.getDestination() != null) {
                        s.setDestination(t.getDestination().getCity());
                        s.setDestinationState(t.getDestination().getState());
                    }
                    s.setBasePrice(t.getBasePrice());
                    s.setTotalSeats(t.getTotalSeats());
                    s.setBookedSeats(t.getBookedSeats());
                    s.setStartDate(t.getStartDate());
                    s.setEndDate(t.getEndDate());
                    s.setCategory(t.getCategory() != null ? t.getCategory().name() : null);
                    s.setStatus(t.getStatus() != null ? t.getStatus().name() : null);
                    return s;
                })
                .collect(Collectors.toList());

        // Build response
        PublicHostResponse response = new PublicHostResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setAvatar(user.getAvatar());
        response.setInstagramUsername(user.getInstagramUsername());
        response.setWebsiteUrl(user.getWebsiteUrl());
        response.setRoleName(user.getRole() != null && user.getRole().getName() != null
                ? user.getRole().getName().name() : null);
        response.setCreatedDate(user.getCreatedAt());
        response.setTotalTrips(totalTrips);
        response.setPublishedTrips(publishedTrips);
        response.setTotalBookings(totalBookings);
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : java.math.BigDecimal.ZERO);
        response.setOccupancyRate(occupancyRate);
        response.setEmailVerified(user.getEmailVerified());
        response.setKycVerified(user.getKycVerified());
        response.setAdminApproved(user.getAdminApproved());
        response.setHostedTrips(hostedTripSummaries);

        return ResponseEntity.ok(ApiResponse.success("Host profile fetched successfully", response));
    }
}
