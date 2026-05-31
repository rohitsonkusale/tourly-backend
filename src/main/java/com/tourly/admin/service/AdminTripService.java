package com.tourly.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.enums.TripStatus;

public interface AdminTripService {

    // =========================================
    // MONITORING
    // =========================================
    Page<TripResponse> getAllTrips(Pageable pageable);

    Page<TripResponse> getDeletedTrips(Pageable pageable);

    Page<TripResponse> getActiveTrips(Pageable pageable);

    Page<TripResponse> getInactiveTrips(Pageable pageable);

    Page<TripResponse> getTripsByStatus(TripStatus status, Pageable pageable);

    TripResponse getTripById(Long tripId);

    // =========================================
    // APPROVAL QUEUE
    // =========================================
    List<TripResponse> getPendingApprovalTrips();

    // =========================================
    // MODERATION ACTIONS
    // =========================================
    TripResponse approveTrip(Long tripId);

    TripResponse rejectTrip(Long tripId, String adminMessage);

    TripResponse markTripPendingReview(Long tripId, String adminMessage);

    TripResponse deactivateTrip(Long tripId);

    TripResponse reactivateTrip(Long tripId);

    TripResponse markTripAsDisputed(Long tripId);
}