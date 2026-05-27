package com.tourly.trip.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.AccountStatus;
import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.trip.dto.request.CreateTripRequest;
import com.tourly.trip.dto.request.UpdateTripRequest;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.entity.Destination;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.enums.TripStatus;
import com.tourly.trip.mapper.TripMapper;
import com.tourly.trip.repository.DestinationRepository;
import com.tourly.trip.repository.TripRepository;
import com.tourly.trip.service.TripService;
import com.tourly.verification.entity.PlannerVerification;
import com.tourly.verification.enums.VerificationStatus;
import com.tourly.verification.repository.PlannerVerificationRepository;
import com.tourly.trip.dto.response.HostStatsResponse;
import com.tourly.booking.repository.BookingRepository;

@Service
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final DestinationRepository destinationRepository;
    private final UserRepository userRepository;
    private final PlannerVerificationRepository plannerVerificationRepository;
    private final BookingRepository bookingRepository;

    public TripServiceImpl(
            TripRepository tripRepository,
            DestinationRepository destinationRepository,
            UserRepository userRepository,
            PlannerVerificationRepository plannerVerificationRepository,
            BookingRepository bookingRepository) {
        this.tripRepository = tripRepository;
        this.destinationRepository = destinationRepository;
        this.userRepository = userRepository;
        this.plannerVerificationRepository = plannerVerificationRepository;
        this.bookingRepository = bookingRepository;
    }

    // ========================================
    // CREATE TRIP
    // Only HOST (verified) or ADMIN
    // NOTE:
    // - HOST can create trip
    // - PLANNER cannot create trip (planner can only plan/suggest)
    // - HOST and PLANNER both require verification in your platform,
    //   but only HOST creation is enforced here
    // ========================================
    @Override
    public TripResponse createTrip(CreateTripRequest request) {
        User currentUser = getCurrentUser();

        RoleName roleName = getRoleName(currentUser);

        // Only HOST or ADMIN can create trip
        if (roleName != RoleName.HOST && roleName != RoleName.ADMIN) {
            throw new BadRequestException("Only verified hosts can create trips.");
        }

        // Admin bypasses verification
        if (roleName != RoleName.ADMIN) {
            validateVerifiedUser(currentUser);
        }

        Destination destination = destinationRepository.findById(request.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination not found with ID: " + request.getDestinationId()));

        validateTripDates(request.getStartDate(), request.getEndDate());
        validateTripPricing(request.getBasePrice(), request.getMinPrice(), request.getMaxPrice());

        Trip trip = new Trip();
        trip.setTitle(request.getTitle().trim());
        trip.setDescription(request.getDescription().trim());

        // Keeping existing field name planner as per your entity design
        trip.setPlanner(currentUser);

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

        // Business defaults
        trip.setStatus(TripStatus.DRAFT);
        trip.setActive(true);
        trip.setDeleted(false);

        Trip savedTrip = tripRepository.save(trip);

        return TripMapper.mapToResponse(savedTrip);
    }

    // ========================================
    // GET ALL TRIPS (PUBLIC)
    // Only published + active + not deleted
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getAllTrips(Pageable pageable) {
        return tripRepository
                .findByDeletedFalseAndActiveTrueAndStatus(TripStatus.PUBLISHED, pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // GET TRIP BY ID
    // Public can only see published + active + not deleted
    // Owner/Admin can see own trip even if draft/inactive
    // Deleted trips remain hidden from public
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        // Deleted trips are hidden from public
        if (Boolean.TRUE.equals(trip.getDeleted())) {
            User currentUser = getCurrentUserOptional();

            if (currentUser == null) {
                throw new ResourceNotFoundException("Trip not found with ID: " + tripId);
            }

            boolean isAdmin = getRoleName(currentUser) == RoleName.ADMIN;
            boolean isOwner = trip.getPlanner() != null && trip.getPlanner().getId().equals(currentUser.getId());

            if (!isAdmin && !isOwner) {
                throw new ResourceNotFoundException("Trip not found with ID: " + tripId);
            }

            return TripMapper.mapToResponse(trip);
        }

        boolean isPublicVisible = Boolean.TRUE.equals(trip.getActive())
                && TripStatus.PUBLISHED.equals(trip.getStatus());

        if (isPublicVisible) {
            return TripMapper.mapToResponse(trip);
        }

        // If not public visible, allow only owner/admin
        User currentUser = getCurrentUserOptional();
        if (currentUser == null) {
            throw new ResourceNotFoundException("Trip not found with ID: " + tripId);
        }

        boolean isAdmin = getRoleName(currentUser) == RoleName.ADMIN;
        boolean isOwner = trip.getPlanner() != null && trip.getPlanner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new ResourceNotFoundException("Trip not found with ID: " + tripId);
        }

        return TripMapper.mapToResponse(trip);
    }

    // ========================================
    // SEARCH TRIPS (PUBLIC)
    // Only published + active + not deleted
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> searchTrips(
            String destination,
            String host,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date.");
        }

        return tripRepository
                .searchPublishedTrips(destination, host, startDate, endDate, TripStatus.PUBLISHED, pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - ALL
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyTrips(Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByPlannerId(currentUser.getId(), pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - ACTIVE
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyActiveTrips(Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByPlannerIdAndActiveTrueAndDeletedFalse(currentUser.getId(), pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - INACTIVE
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyInactiveTrips(Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByPlannerIdAndActiveFalseAndDeletedFalse(currentUser.getId(), pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - DELETED
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyDeletedTrips(Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByPlannerIdAndDeletedTrue(currentUser.getId(), pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - BY STATUS
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyTripsByStatus(TripStatus status, Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByPlannerIdAndStatusAndDeletedFalse(currentUser.getId(), status, pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // UPDATE TRIP
    // Owner or Admin
    // ========================================
    @Override
    public TripResponse updateTrip(Long tripId, UpdateTripRequest request) {
        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        validateTripAccess(trip, currentUser);

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new BadRequestException("Deleted trip cannot be updated.");
        }

        if (request.getTitle() != null) {
            trip.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            trip.setDescription(request.getDescription().trim());
        }

        if (request.getDestinationId() != null) {
            Destination destination = destinationRepository.findById(request.getDestinationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Destination not found with ID: " + request.getDestinationId()));
            trip.setDestination(destination);
        }

        LocalDate newStartDate = request.getStartDate() != null ? request.getStartDate() : trip.getStartDate();
        LocalDate newEndDate = request.getEndDate() != null ? request.getEndDate() : trip.getEndDate();
        validateTripDates(newStartDate, newEndDate);

        trip.setStartDate(newStartDate);
        trip.setEndDate(newEndDate);

        if (request.getBasePrice() != null || request.getMinPrice() != null || request.getMaxPrice() != null) {
            var newBasePrice = request.getBasePrice() != null ? request.getBasePrice() : trip.getBasePrice();
            var newMinPrice = request.getMinPrice() != null ? request.getMinPrice() : trip.getMinPrice();
            var newMaxPrice = request.getMaxPrice() != null ? request.getMaxPrice() : trip.getMaxPrice();

            validateTripPricing(newBasePrice, newMinPrice, newMaxPrice);

            trip.setBasePrice(newBasePrice);
            trip.setMinPrice(newMinPrice);
            trip.setMaxPrice(newMaxPrice);
        }

        if (request.getTotalSeats() != null) {
            if (trip.getBookedSeats() != null && request.getTotalSeats() < trip.getBookedSeats()) {
                throw new BadRequestException("Total seats cannot be less than already booked seats.");
            }
            trip.setTotalSeats(request.getTotalSeats());
        }

        if (request.getCategory() != null) {
            trip.setCategory(request.getCategory());
        }

        if (request.getCancellationPolicy() != null) {
            trip.setCancellationPolicy(request.getCancellationPolicy());
        }

        if (request.getStatus() != null) {
            trip.setStatus(request.getStatus());
        }

        if (request.getActive() != null) {
            trip.setActive(request.getActive());
        }

        Trip updatedTrip = tripRepository.save(trip);
        return TripMapper.mapToResponse(updatedTrip);
    }

    // ========================================
    // DELETE TRIP (SOFT DELETE)
    // Owner or Admin
    // ========================================
    @Override
    public void deleteTrip(Long tripId) {
        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        validateTripAccess(trip, currentUser);

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new BadRequestException("Trip is already deleted.");
        }

        trip.setActive(false);
        trip.setDeleted(true);
        trip.setDeletedAt(LocalDateTime.now());

        tripRepository.save(trip);
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Authenticated user not found.");
        }

        String email = authentication.getName();

        if ("anonymousUser".equalsIgnoreCase(email)) {
            throw new BadRequestException("Authenticated user not found.");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private User getCurrentUserOptional() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getName() == null) {
                return null;
            }

            String email = authentication.getName();

            if ("anonymousUser".equalsIgnoreCase(email)) {
                return null;
            }

            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    // IMPORTANT:
    // User -> Role -> RoleName
    private RoleName getRoleName(User user) {
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            throw new BadRequestException("User role is not assigned properly.");
        }
        return user.getRole().getName();
    }

    // Verification required for HOST / PLANNER on your platform
    private void validateVerifiedUser(User user) {
        RoleName roleName = getRoleName(user);

        if (roleName != RoleName.HOST && roleName != RoleName.PLANNER) {
            throw new BadRequestException("Verification is only applicable for host or planner accounts.");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Your account is not active. You cannot create trips.");
        }

        if (Boolean.FALSE.equals(user.getKycVerified())) {
            throw new BadRequestException("Your KYC is not verified. You cannot create trips.");
        }

        PlannerVerification verification = plannerVerificationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "Verification profile not found. Please complete verification first."));

        if (verification.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new BadRequestException("Your verification is not approved yet. You cannot create trips.");
        }
    }

    private void validateTripDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required.");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date.");
        }
    }

    private void validateTripPricing(
            java.math.BigDecimal basePrice,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice) {

        if (basePrice == null || minPrice == null || maxPrice == null) {
            throw new BadRequestException("Base price, minimum price, and maximum price are required.");
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Minimum price cannot be greater than maximum price.");
        }

        if (basePrice.compareTo(minPrice) < 0 || basePrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Base price must be between minimum price and maximum price.");
        }
    }

    private void validateTripAccess(Trip trip, User currentUser) {
        boolean isAdmin = getRoleName(currentUser) == RoleName.ADMIN;
        boolean isOwner = trip.getPlanner() != null && trip.getPlanner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new BadRequestException("You are not allowed to manage this trip.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HostStatsResponse getHostStats() {
        User currentUser = getCurrentUser();

        // 1. Upcoming Trips Count (active, non-deleted, starts in future)
        long upcomingTrips = tripRepository.countByPlannerIdAndActiveTrueAndDeletedFalseAndStartDateAfter(
                currentUser.getId(),
                LocalDate.now()
        );

        // 2. Total Bookings Count (confirmed / completed bookings on host's trips)
        long totalBookings = bookingRepository.countBookingsByHostId(currentUser.getId());

        // 3. Monthly Earnings (total earnings from host's trips)
        java.math.BigDecimal earnings = bookingRepository.sumEarningsByHostId(currentUser.getId());

        // 4. Pending Actions Count (trips that are DRAFT / pending review)
        long pendingTrips = tripRepository.countByPlannerIdAndActiveTrueAndDeletedFalseAndStatus(
                currentUser.getId(),
                TripStatus.DRAFT
        );

        return new HostStatsResponse(upcomingTrips, totalBookings, earnings, pendingTrips);
    }
}