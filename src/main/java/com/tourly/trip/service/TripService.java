package com.tourly.trip.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tourly.trip.dto.request.CreateTripRequest;
import com.tourly.trip.dto.request.UpdateTripRequest;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.enums.TripStatus;

public interface TripService {

    TripResponse createTrip(CreateTripRequest request);

    Page<TripResponse> getAllTrips(Pageable pageable);

    TripResponse getTripById(Long tripId);

    Page<TripResponse> searchTrips(
            String destination,
            String host,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    TripResponse updateTrip(Long tripId, UpdateTripRequest request);

    void deleteTrip(Long tripId);

    // =========================================
    // PLANNER / HOST - MY TRIPS
    // =========================================
    Page<TripResponse> getMyTrips(Pageable pageable);

    Page<TripResponse> getMyActiveTrips(Pageable pageable);

    Page<TripResponse> getMyInactiveTrips(Pageable pageable);

    Page<TripResponse> getMyDeletedTrips(Pageable pageable);

    Page<TripResponse> getMyTripsByStatus(TripStatus status, Pageable pageable);
}