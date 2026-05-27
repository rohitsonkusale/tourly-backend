package com.tourly.booking.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;

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
    // ADMIN BOOKING FILTER
    // =====================================
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

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

    // =====================================
    // HOST DASHBOARD STATS
    // =====================================
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.trip.planner.id = :hostId
          AND (b.status = 'CONFIRMED' OR b.status = 'COMPLETED')
    """)
    long countBookingsByHostId(@Param("hostId") Long hostId);

    @Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b
        WHERE b.trip.planner.id = :hostId
          AND (b.status = 'CONFIRMED' OR b.status = 'COMPLETED')
    """)
    java.math.BigDecimal sumEarningsByHostId(@Param("hostId") Long hostId);
}