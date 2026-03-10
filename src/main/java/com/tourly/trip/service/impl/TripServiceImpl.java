package com.tourly.trip.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tourly.trip.dto.request.CreateTripRequest;
import com.tourly.trip.dto.request.UpdateTripRequest;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.entity.Destination;
import com.tourly.trip.mapper.TripMapper;
import com.tourly.trip.repository.TripRepository;
import com.tourly.trip.repository.DestinationRepository;
import com.tourly.trip.service.TripService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;

import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;

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

    @Override
    public TripResponse createTrip(CreateTripRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User planner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check role using ENUM comparison
        RoleName role = planner.getRole().getName();

        if (role != RoleName.PLANNER && role != RoleName.HOST) {
            throw new RuntimeException("Only planners or hosts can create trips");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }

        // Validate seats
        if (request.getTotalSeats() <= 0) {
            throw new RuntimeException("Total seats must be greater than 0");
        }

        Destination destination = destinationRepository
                .findById(request.getDestinationId())
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

        Trip savedTrip = tripRepository.save(trip);

        return TripMapper.mapToResponse(savedTrip);
    }

    @Override
    public Page<TripResponse> getAllTrips(Pageable pageable) {

        Page<Trip> trips = tripRepository.findAll(pageable);

        return trips.map(TripMapper::mapToResponse);
    }

    @Override
    public TripResponse getTripById(Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        return TripMapper.mapToResponse(trip);
    }

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

    @Override
    public TripResponse updateTrip(Long tripId, UpdateTripRequest request) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if(request.getTitle() != null)
            trip.setTitle(request.getTitle());

        if(request.getDescription() != null)
            trip.setDescription(request.getDescription());

        if(request.getStartDate() != null)
            trip.setStartDate(request.getStartDate());

        if(request.getEndDate() != null)
            trip.setEndDate(request.getEndDate());

        if(request.getBasePrice() != null)
            trip.setBasePrice(request.getBasePrice());

        if(request.getMinPrice() != null)
            trip.setMinPrice(request.getMinPrice());

        if(request.getMaxPrice() != null)
            trip.setMaxPrice(request.getMaxPrice());

        if(request.getTotalSeats() != null)
            trip.setTotalSeats(request.getTotalSeats());

        Trip updatedTrip = tripRepository.save(trip);

        return TripMapper.mapToResponse(updatedTrip);
    }

    @Override
    public void deleteTrip(Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        trip.setActive(true);
        trip.setDeleted(false);

        tripRepository.save(trip);
    }
}