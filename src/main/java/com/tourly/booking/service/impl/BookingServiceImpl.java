package com.tourly.booking.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.dto.request.CancelBookingRequest;
import com.tourly.booking.dto.request.CreateBookingRequest;
import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.enums.PaymentStatus;
import com.tourly.booking.mapper.BookingMapper;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.booking.service.BookingService;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.repository.TripRepository;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            TripRepository tripRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    // =====================================
    // HELPER: GET CURRENT USER
    // =====================================
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =====================================
    // HELPER: CHECK BOOKING OWNERSHIP
    // =====================================
    private void validateBookingOwnership(Booking booking, User currentUser) {
        if (booking.getTraveler() == null || booking.getTraveler().getId() == null) {
            throw new RuntimeException("Booking traveler not found");
        }

        if (!booking.getTraveler().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to access this booking");
        }
    }

    // =====================================
    // HELPER: CHECK TRIP OWNERSHIP FOR PLANNER/HOST
    // =====================================
    private void validateTripOwnership(Trip trip, User currentUser) {
        if (trip.getPlanner() == null || trip.getPlanner().getId() == null) {
            throw new RuntimeException("Trip owner not found");
        }

        if (!trip.getPlanner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to view bookings for this trip");
        }
    }

    // =====================================
    // BOOK TRIP (Race-condition safe)
    // =====================================
    @Override
    @Transactional
    public BookingResponse bookTrip(CreateBookingRequest request) {

        User traveler = getCurrentUser();

        // Lock trip row to prevent race condition
        Trip trip = tripRepository.findTripForUpdate(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Trip must be active and not deleted
        if (Boolean.TRUE.equals(trip.getDeleted()) || !Boolean.TRUE.equals(trip.getActive())) {
            throw new RuntimeException("Trip is not available for booking");
        }

        // Prevent booking past/ended trip
        if (trip.getEndDate() != null && trip.getEndDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("This trip has already ended");
        }

        // Validate seats
        if (request.getSeats() == null || request.getSeats() <= 0) {
            throw new RuntimeException("Seats must be greater than 0");
        }

        // Null safety for seat fields
        if (trip.getTotalSeats() == null || trip.getBookedSeats() == null) {
            throw new RuntimeException("Trip seat configuration is invalid");
        }

        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();

        if (request.getSeats() > availableSeats) {
            throw new RuntimeException("Not enough seats available");
        }

        // Optional business rule: prevent planner from booking own trip
        if (trip.getPlanner() != null
                && trip.getPlanner().getId() != null
                && trip.getPlanner().getId().equals(traveler.getId())) {
            throw new RuntimeException("You cannot book your own trip");
        }

        // Price validation
        if (trip.getBasePrice() == null) {
            throw new RuntimeException("Trip price is not configured");
        }

        // Reserve seats temporarily
        trip.setBookedSeats(trip.getBookedSeats() + request.getSeats());
        tripRepository.save(trip);

        BigDecimal totalPrice =
                trip.getBasePrice().multiply(BigDecimal.valueOf(request.getSeats()));

        Booking booking = new Booking();

        booking.setTrip(trip);
        booking.setTraveler(traveler);
        booking.setSeatsBooked(request.getSeats());
        booking.setTotalPrice(totalPrice);

        // Booking initially pending
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        // Payment must complete within 10 minutes
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        Booking savedBooking = bookingRepository.save(booking);

        return BookingMapper.toResponse(savedBooking);
    }

    // =====================================
    // TRAVELER BOOKINGS
    // =====================================
    @Override
    public List<BookingResponse> getMyBookings() {

        User traveler = getCurrentUser();

        List<Booking> bookings = bookingRepository.findByTravelerId(traveler.getId());

        return bookings.stream()
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================
    // BOOKINGS FOR A TRIP (OWNER ONLY)
    // =====================================
    @Override
    public List<BookingResponse> getTripBookings(Long tripId) {

        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new RuntimeException("Trip not found");
        }

        // Only owner planner/host can see trip bookings
        validateTripOwnership(trip, currentUser);

        List<Booking> bookings = bookingRepository.findByTripId(tripId);

        return bookings.stream()
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================
    // CANCEL BOOKING (OWNER ONLY)
    // =====================================
    @Override
    @Transactional
    public void cancelBooking(Long bookingId, CancelBookingRequest request) {

        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Only traveler who owns booking can cancel
        validateBookingOwnership(booking, currentUser);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Completed booking cannot be cancelled");
        }

        Trip trip = booking.getTrip();

        if (trip == null) {
            throw new RuntimeException("Trip not found for this booking");
        }

        if (trip.getBookedSeats() == null) {
            throw new RuntimeException("Trip seat data is invalid");
        }

        if (booking.getSeatsBooked() == null) {
            throw new RuntimeException("Booking seat data is invalid");
        }

        // Release seats safely
        int updatedSeats = trip.getBookedSeats() - booking.getSeatsBooked();
        trip.setBookedSeats(Math.max(updatedSeats, 0));
        tripRepository.save(trip);

        booking.setStatus(BookingStatus.CANCELLED);

        // If payment already done, later refund flow can handle it
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            booking.setPaymentStatus(PaymentStatus.FAILED);
        }

        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }
}