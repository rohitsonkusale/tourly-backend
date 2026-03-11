package com.tourly.booking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tourly.booking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // =====================================
    // Traveler bookings
    // =====================================
    List<Booking> findByTravelerId(Long travelerId);

    // =====================================
    // Planner trip bookings
    // =====================================
    List<Booking> findByTripId(Long tripId);

    // =====================================
    // Find expired bookings for scheduler
    // =====================================
    @Query("""
    SELECT b FROM Booking b
    WHERE b.status = 'PENDING'
    AND b.paymentStatus = 'PENDING'
    AND b.expiresAt < :now
    """)
    List<Booking> findExpiredBookings(LocalDateTime now);

}