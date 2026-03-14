package com.tourly.booking.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tourly.booking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // =====================================
    // TRAVELER BOOKINGS
    // =====================================
    List<Booking> findByTravelerId(Long travelerId);

    // =====================================
    // BOOKINGS FOR A TRIP
    // =====================================
    List<Booking> findByTripId(Long tripId);

    // =====================================
    // FIND EXPIRED PENDING BOOKINGS (Scheduler)
    // =====================================
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.status = 'PENDING'
          AND b.paymentStatus = 'PENDING'
          AND b.expiresAt < :now
    """)
    List<Booking> findExpiredBookings(@Param("now") LocalDateTime now);

    // =====================================
    // FIND COMPLETED TRIPS (Scheduler)
    // =====================================
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.status = 'CONFIRMED'
          AND b.trip.endDate < :today
    """)
    List<Booking> findCompletedTrips(@Param("today") LocalDate today);
}