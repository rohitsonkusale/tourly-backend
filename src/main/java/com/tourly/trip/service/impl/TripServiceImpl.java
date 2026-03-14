package com.tourly.trip.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.trip.dto.request.CreateTripRequest;
import com.tourly.trip.dto.request.UpdateTripRequest;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.entity.Destination;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.mapper.TripMapper;
import com.tourly.trip.repository.DestinationRepository;
import com.tourly.trip.repository.TripRepository;
import com.tourly.trip.service.TripService;

@Service
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final DestinationRepository destinationRepository;
    private final UserRepository userRepository;

    public TripServiceImpl(
            TripRepository tripRepository,
            DestinationRepository destinationRepository,
            UserRepository userRepository) {

        this.tripRepository = tripRepository;
        this.destinationRepository = destinationRepository;
        this.userRepository = userRepository;
    }

    // =========================================
    // HELPER: GET CURRENT LOGGED-IN USER
    // =========================================
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =========================================
    // HELPER: CHECK TRIP OWNERSHIP
    // =========================================
    private void validateTripOwnership(Trip trip, User currentUser) {
        if (trip.getPlanner() == null || trip.getPlanner().getId() == null) {
            throw new RuntimeException("Trip owner not found");
        }

        if (!trip.getPlanner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to modify this trip");
        }
    }

    // =========================================
    // HELPER: VALIDATE PRICE RANGE
    // =========================================
    private void validatePriceRange(Trip trip) {
        if (trip.getBasePrice() == null || trip.getMinPrice() == null || trip.getMaxPrice() == null) {
            throw new RuntimeException("Price fields cannot be null");
        }

        if (trip.getBasePrice().compareTo(trip.getMinPrice()) < 0) {
            throw new RuntimeException("Base price cannot be less than minimum price");
        }

        if (trip.getBasePrice().compareTo(trip.getMaxPrice()) > 0) {
            throw new RuntimeException("Base price cannot be greater than maximum price");
        }

        if (trip.getMinPrice().compareTo(trip.getMaxPrice()) > 0) {
            throw new RuntimeException("Minimum price cannot be greater than maximum price");
        }
    }

    // =========================================
    // HELPER: VALIDATE DATES
    // =========================================
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }
    }

    // =========================================
    // CREATE TRIP (PLANNER / HOST)
    // =========================================
    @Override
    public TripResponse createTrip(CreateTripRequest request) {

        User planner = getCurrentUser();

        RoleName role = planner.getRole().getName();

        if (role != RoleName.PLANNER && role != RoleName.HOST) {
            throw new RuntimeException("Only planners or hosts can create trips");
        }

        // Validate dates
        validateDates(request.getStartDate(), request.getEndDate());

        // Validate seats
        if (request.getTotalSeats() == null || request.getTotalSeats() <= 0) {
            throw new RuntimeException("Total seats must be greater than 0");
        }

        Destination destination = destinationRepository.findById(request.getDestinationId())
                .orElseThrow(() -> new RuntimeException("Destination not found"));

        Trip trip = new Trip();

        trip.setTitle(request.getTitle());
        trip.setDescription(request.getDescription());
        trip.setPlanner(planner);
        trip.setDestination(destination);
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());

        trip.setBasePrice(request.getBasePrice());
        trip.setMinPrice(request.getMinPrice());
        trip.setMaxPrice(request.getMaxPrice());

        trip.setTotalSeats(request.getTotalSeats());
        trip.setBookedSeats(0);

        trip.setCategory(request.getCategory());
        trip.setCancellationPolicy(request.getCancellationPolicy());

        trip.setActive(true);
        trip.setDeleted(false);

        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        // Validate price rules
        validatePriceRange(trip);

        Trip savedTrip = tripRepository.save(trip);

        return TripMapper.mapToResponse(savedTrip);
    }

    // =========================================
    // GET ALL TRIPS (PUBLIC - ONLY ACTIVE)
    // =========================================
    @Override
    public Page<TripResponse> getAllTrips(Pageable pageable) {

        Page<Trip> trips = tripRepository.findByDeletedFalseAndActiveTrue(pageable);

        return trips.map(TripMapper::mapToResponse);
    }

    // =========================================
    // GET TRIP BY ID (PUBLIC - ONLY ACTIVE)
    // =========================================
    @Override
    public TripResponse getTripById(Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (Boolean.TRUE.equals(trip.getDeleted()) || Boolean.FALSE.equals(trip.getActive())) {
            throw new RuntimeException("Trip not found or no longer available");
        }

        return TripMapper.mapToResponse(trip);
    }

    // =========================================
    // SEARCH TRIPS (PUBLIC - SHOULD RETURN ONLY ACTIVE)
    // =========================================
    @Override
    public Page<TripResponse> searchTrips(
            String destination,
            String host,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        Page<Trip> trips = tripRepository.searchTrips(
                destination,
                host,
                startDate,
                endDate,
                pageable
        );

        return trips.map(TripMapper::mapToResponse);
    }

    // =========================================
    // UPDATE TRIP (OWNER ONLY)
    // =========================================
    @Override
    public TripResponse updateTrip(Long tripId, UpdateTripRequest request) {

        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (Boolean.TRUE.equals(trip.getDeleted()) || Boolean.FALSE.equals(trip.getActive())) {
            throw new RuntimeException("Cannot update deleted or inactive trip");
        }

        // Ownership check
        validateTripOwnership(trip, currentUser);

        if (request.getTitle() != null) {
            trip.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            trip.setDescription(request.getDescription());
        }

        if (request.getStartDate() != null) {
            trip.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            trip.setEndDate(request.getEndDate());
        }

        if (request.getBasePrice() != null) {
            trip.setBasePrice(request.getBasePrice());
        }

        if (request.getMinPrice() != null) {
            trip.setMinPrice(request.getMinPrice());
        }

        if (request.getMaxPrice() != null) {
            trip.setMaxPrice(request.getMaxPrice());
        }

        if (request.getTotalSeats() != null) {
            if (request.getTotalSeats() <= 0) {
                throw new RuntimeException("Total seats must be greater than 0");
            }

            if (request.getTotalSeats() < trip.getBookedSeats()) {
                throw new RuntimeException("Total seats cannot be less than already booked seats");
            }

            trip.setTotalSeats(request.getTotalSeats());
        }

        // Validate dates after applying updates
        validateDates(trip.getStartDate(), trip.getEndDate());

        // Validate price range after applying updates
        validatePriceRange(trip);

        trip.setUpdatedAt(LocalDateTime.now());

        Trip updatedTrip = tripRepository.save(trip);

        return TripMapper.mapToResponse(updatedTrip);
    }

    // =========================================
    // DELETE TRIP (SOFT DELETE - OWNER ONLY)
    // =========================================
    @Override
    public void deleteTrip(Long tripId) {

        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new RuntimeException("Trip is already deleted");
        }

        // Ownership check
        validateTripOwnership(trip, currentUser);

        // Soft delete
        trip.setActive(false);
        trip.setDeleted(true);
        trip.setUpdatedAt(LocalDateTime.now());

        tripRepository.save(trip);
    }
}