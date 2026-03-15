package com.tourly.admin.service;

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
    // MODERATION
    // =========================================
    TripResponse deactivateTrip(Long tripId);

    TripResponse reactivateTrip(Long tripId);

    TripResponse markTripAsDisputed(Long tripId);
}