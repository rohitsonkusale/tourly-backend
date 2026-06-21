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

    List<Booking> findByTravelerIdOrderByCreatedAtDesc(Long travelerId);

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
        WHERE (b.trip.host.id = :hostId OR b.trip.planner.id = :hostId)
          AND (b.status = 'CONFIRMED' OR b.status = 'COMPLETED')
    """)
    long countBookingsByHostId(@Param("hostId") Long hostId);

    @Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b
        WHERE (b.trip.host.id = :hostId OR b.trip.planner.id = :hostId)
          AND (b.status = 'CONFIRMED' OR b.status = 'COMPLETED')
    """)
    java.math.BigDecimal sumEarningsByHostId(@Param("hostId") Long hostId);

    // =====================================
    // ADMIN DASHBOARD STATS
    // =====================================

    @Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b
        WHERE b.status IN ('CONFIRMED', 'COMPLETED')
    """)
    java.math.BigDecimal sumTotalRevenue();

    @Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b
        WHERE b.status IN ('CONFIRMED', 'COMPLETED')
          AND YEAR(b.createdAt) = :year
          AND MONTH(b.createdAt) = :month
    """)
    java.math.BigDecimal sumRevenueForMonth(@Param("year") int year, @Param("month") int month);

    long countByStatusIn(java.util.List<BookingStatus> statuses);

    @Query("""
        SELECT CAST(b.createdAt AS LocalDate), COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE b.status IN ('CONFIRMED', 'COMPLETED')
          AND b.createdAt >= :from
        GROUP BY CAST(b.createdAt AS LocalDate)
        ORDER BY CAST(b.createdAt AS LocalDate) ASC
    """)
    java.util.List<Object[]> sumDailyRevenue(@Param("from") LocalDateTime from);

    @Query("""
        SELECT b.trip.destination.state, COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE b.status IN ('CONFIRMED', 'COMPLETED')
          AND b.trip.destination.state IS NOT NULL
        GROUP BY b.trip.destination.state
        ORDER BY SUM(b.totalPrice) DESC
    """)
    java.util.List<Object[]> sumRevenueByDestinationState();

    @Query("""
        SELECT b.trip.destination.city, COUNT(b)
        FROM Booking b
        WHERE b.status IN ('CONFIRMED', 'COMPLETED')
          AND b.trip.destination.city IS NOT NULL
        GROUP BY b.trip.destination.city
        ORDER BY COUNT(b) DESC
    """)
    java.util.List<Object[]> countBookingsByDestination();

    @Query("""
        SELECT b.trip.planner.fullName, COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE b.status IN ('CONFIRMED', 'COMPLETED')
          AND b.trip.planner IS NOT NULL
        GROUP BY b.trip.planner.id, b.trip.planner.fullName
        ORDER BY SUM(b.totalPrice) DESC
    """)
    java.util.List<Object[]> sumRevenueByHost();

    @Query("""
        SELECT COUNT(DISTINCT sub.travelerId) FROM (
            SELECT b.traveler.id AS travelerId
            FROM Booking b
            WHERE b.status IN ('CONFIRMED', 'COMPLETED')
            GROUP BY b.traveler.id
            HAVING COUNT(b) > 1
        ) sub
    """)
    long countRepeatTravelers();

    @Query("""
        SELECT COUNT(DISTINCT b.traveler.id) FROM Booking b
        WHERE b.status IN ('CONFIRMED', 'COMPLETED')
    """)
    long countDistinctTravelers();

    // =====================================
    // HOST ANALYTICS
    // =====================================
    @Query("""
        SELECT b.trip.id, b.trip.title,
               b.trip.destination.city,
               COUNT(b),
               COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE (b.trip.host.id = :hostId OR b.trip.planner.id = :hostId)
          AND b.status IN ('CONFIRMED', 'COMPLETED')
        GROUP BY b.trip.id, b.trip.title, b.trip.destination.city
        ORDER BY COUNT(b) DESC
    """)
    java.util.List<Object[]> findTopTripsByHostId(@Param("hostId") Long hostId);

    // =====================================
    // HOST PAYMENTS — all bookings on host's trips
    // =====================================
    @Query("""
        SELECT b FROM Booking b
        WHERE (b.trip.host.id = :hostId OR b.trip.planner.id = :hostId)
        ORDER BY b.createdAt DESC
    """)
    java.util.List<Booking> findAllBookingsByHostId(@Param("hostId") Long hostId);
}