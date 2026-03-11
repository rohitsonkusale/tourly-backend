package com.tourly.booking.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.dto.request.CreateBookingRequest;
import com.tourly.booking.dto.request.CancelBookingRequest;
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
    // BOOK TRIP (Race-condition safe)
    // =====================================
    @Override
    @Transactional
    public BookingResponse bookTrip(CreateBookingRequest request) {

        // Lock trip row to prevent race condition
        Trip trip = tripRepository.findTripForUpdate(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User traveler = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();

        if (request.getSeats() > availableSeats) {
            throw new RuntimeException("Not enough seats available");
        }

        // reserve seats temporarily
        trip.setBookedSeats(trip.getBookedSeats() + request.getSeats());

        BigDecimal totalPrice =
                trip.getBasePrice().multiply(BigDecimal.valueOf(request.getSeats()));

        Booking booking = new Booking();

        booking.setTrip(trip);
        booking.setTraveler(traveler);
        booking.setSeatsBooked(request.getSeats());
        booking.setTotalPrice(totalPrice);

        // booking initially pending
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        booking.setCreatedAt(LocalDateTime.now());

        // payment must complete within 10 minutes
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        bookingRepository.save(booking);

        return BookingMapper.toResponse(booking);
    }

    // =====================================
    // TRAVELER BOOKINGS
    // =====================================
    @Override
    public List<BookingResponse> getMyBookings() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User traveler = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings =
                bookingRepository.findByTravelerId(traveler.getId());

        return bookings.stream()
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================
    // BOOKINGS FOR A TRIP (PLANNER)
    // =====================================
    @Override
    public List<BookingResponse> getTripBookings(Long tripId) {

        List<Booking> bookings =
                bookingRepository.findByTripId(tripId);

        return bookings.stream()
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================
    // CANCEL BOOKING
    // =====================================
    @Override
    @Transactional
    public void cancelBooking(Long bookingId, CancelBookingRequest request) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking already cancelled");
        }

        Trip trip = booking.getTrip();

        // release seats
        trip.setBookedSeats(
                trip.getBookedSeats() - booking.getSeatsBooked()
        );

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.FAILED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }
}