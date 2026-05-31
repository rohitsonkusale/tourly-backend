package com.tourly.admin.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tourly.admin.service.AdminTripService;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.enums.ApprovalStatus;
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
                .map(TripMapper::mapToResponse);
    }

    @Override
    public Page<TripResponse> getDeletedTrips(Pageable pageable) {
        return tripRepository.findByDeletedTrue(pageable)
                .map(TripMapper::mapToResponse);
    }

    @Override
    public Page<TripResponse> getActiveTrips(Pageable pageable) {
        return tripRepository.findByActiveTrueAndDeletedFalse(pageable)
                .map(TripMapper::mapToResponse);
    }

    @Override
    public Page<TripResponse> getInactiveTrips(Pageable pageable) {
        return tripRepository.findByActiveFalseAndDeletedFalse(pageable)
                .map(TripMapper::mapToResponse);
    }

    @Override
    public Page<TripResponse> getTripsByStatus(TripStatus status, Pageable pageable) {
        return tripRepository.findByStatusAndDeletedFalse(status, pageable)
                .map(TripMapper::mapToResponse);
    }

    @Override
    public TripResponse getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));
        return TripMapper.mapToResponse(trip);
    }

    // ===============================
    // APPROVAL QUEUE
    // ===============================

    @Override
    public List<TripResponse> getPendingApprovalTrips() {
        return tripRepository
                .findByApprovalStatusAndDeletedFalse(ApprovalStatus.PENDING)
                .stream()
                .map(TripMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    // ===============================
    // MODERATION ACTIONS
    // ===============================

    @Override
    public TripResponse approveTrip(Long tripId) {
        Trip trip = getActiveTrip(tripId);

        trip.setApprovalStatus(ApprovalStatus.APPROVED);
        trip.setRejectionReason(null);
        // Publish the trip so it becomes visible to travelers
        trip.setStatus(TripStatus.PUBLISHED);
        trip.setActive(true);

        return TripMapper.mapToResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse rejectTrip(Long tripId, String adminMessage) {
        if (adminMessage == null || adminMessage.trim().isEmpty()) {
            throw new BadRequestException("Rejection reason is mandatory.");
        }
        Trip trip = getActiveTrip(tripId);

        trip.setApprovalStatus(ApprovalStatus.REJECTED);
        trip.setRejectionReason(adminMessage.trim());
        // Keep as DRAFT — hidden from public
        trip.setStatus(TripStatus.DRAFT);
        trip.setActive(false);

        return TripMapper.mapToResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse markTripPendingReview(Long tripId, String adminMessage) {
        if (adminMessage == null || adminMessage.trim().isEmpty()) {
            throw new BadRequestException("A message explaining required changes is mandatory.");
        }
        Trip trip = getActiveTrip(tripId);

        trip.setApprovalStatus(ApprovalStatus.PENDING_REVIEW);
        trip.setRejectionReason(adminMessage.trim());
        // Keep as DRAFT — hidden from public
        trip.setStatus(TripStatus.DRAFT);
        trip.setActive(false);

        return TripMapper.mapToResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse deactivateTrip(Long tripId) {
        Trip trip = getActiveTrip(tripId);
        trip.setActive(false);
        if (trip.getStatus() == TripStatus.PUBLISHED || trip.getStatus() == TripStatus.CONFIRMED) {
            trip.setStatus(TripStatus.CANCELLED);
        }
        return TripMapper.mapToResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse reactivateTrip(Long tripId) {
        Trip trip = getActiveTrip(tripId);
        trip.setActive(true);
        if (trip.getStatus() == TripStatus.CANCELLED || trip.getStatus() == TripStatus.DISPUTED) {
            trip.setStatus(TripStatus.PUBLISHED);
        }
        return TripMapper.mapToResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse markTripAsDisputed(Long tripId) {
        Trip trip = getActiveTrip(tripId);
        trip.setStatus(TripStatus.DISPUTED);
        trip.setActive(false);
        return TripMapper.mapToResponse(tripRepository.save(trip));
    }

    // ===============================
    // HELPER
    // ===============================
    private Trip getActiveTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));
        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new BadRequestException("Cannot moderate a deleted trip.");
        }
        return trip;
    }
}