package com.tourly.admin.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tourly.admin.service.AdminTripService;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.enums.TripStatus;
import com.tourly.trip.mapper.TripMapper;
import com.tourly.trip.repository.TripRepository;

@Service
public class AdminTripServiceImpl implements AdminTripService {

    private final TripRepository tripRepository;

    public AdminTripServiceImpl(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    // ===============================
    // MONITORING
    // ===============================

    @Override
    public Page<TripResponse> getAllTrips(Pageable pageable) {
        return tripRepository.findByDeletedFalse(pageable)
                .map(TripMapper::mapToAdminResponse);
    }

    @Override
    public Page<TripResponse> getDeletedTrips(Pageable pageable) {
        return tripRepository.findByDeletedTrue(pageable)
                .map(TripMapper::mapToAdminResponse);
    }

    @Override
    public Page<TripResponse> getActiveTrips(Pageable pageable) {
        return tripRepository.findByActiveTrueAndDeletedFalse(pageable)
                .map(TripMapper::mapToAdminResponse);
    }

    @Override
    public Page<TripResponse> getInactiveTrips(Pageable pageable) {
        return tripRepository.findByActiveFalseAndDeletedFalse(pageable)
                .map(TripMapper::mapToAdminResponse);
    }

    @Override
    public Page<TripResponse> getTripsByStatus(TripStatus status, Pageable pageable) {
        return tripRepository.findByStatusAndDeletedFalse(status, pageable)
                .map(TripMapper::mapToAdminResponse);
    }

    @Override
    public TripResponse getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));
        return TripMapper.mapToAdminResponse(trip);
    }

    // ===============================
    // MODERATION
    // ===============================

    @Override
    public TripResponse deactivateTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new RuntimeException("Cannot deactivate a deleted trip");
        }

        trip.setActive(false);

        if (trip.getStatus() == TripStatus.PUBLISHED || trip.getStatus() == TripStatus.CONFIRMED) {
            trip.setStatus(TripStatus.CANCELLED);
        }

        Trip savedTrip = tripRepository.save(trip);
        return TripMapper.mapToAdminResponse(savedTrip);
    }

    @Override
    public TripResponse reactivateTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new RuntimeException("Deleted trip cannot be reactivated");
        }

        trip.setActive(true);

        if (trip.getStatus() == TripStatus.CANCELLED || trip.getStatus() == TripStatus.DISPUTED) {
            trip.setStatus(TripStatus.PUBLISHED);
        }

        Trip savedTrip = tripRepository.save(trip);
        return TripMapper.mapToAdminResponse(savedTrip);
    }

    @Override
    public TripResponse markTripAsDisputed(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new RuntimeException("Deleted trip cannot be marked as disputed");
        }

        trip.setStatus(TripStatus.DISPUTED);
        trip.setActive(false);

        Trip savedTrip = tripRepository.save(trip);
        return TripMapper.mapToAdminResponse(savedTrip);
    }
}