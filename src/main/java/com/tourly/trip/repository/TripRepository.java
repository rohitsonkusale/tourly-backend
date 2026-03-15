package com.tourly.trip.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tourly.trip.entity.Trip;
import com.tourly.trip.enums.TripStatus;

import jakarta.persistence.LockModeType;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // ========================================
    // PUBLIC TRIPS (BACKWARD COMPATIBILITY)
    // Existing method kept so current code doesn't break
    // ========================================
    Page<Trip> findByDeletedFalseAndActiveTrue(Pageable pageable);

    // ========================================
    // PUBLIC TRIPS (STRICT / PRODUCTION-SAFE)
    // Only ACTIVE + NOT DELETED + PUBLISHED
    // Use these in upgraded public APIs
    // ========================================
    Page<Trip> findByDeletedFalseAndActiveTrueAndStatus(
            TripStatus status,
            Pageable pageable
    );

    // ========================================
    // SEARCH TRIPS (CURRENT VERSION - BACKWARD COMPATIBILITY)
    // Existing method kept so current code doesn't break
    // ========================================
    @Query("""
        SELECT t FROM Trip t
        WHERE t.active = true
          AND t.deleted = false
          AND (:destination IS NULL OR LOWER(t.destination.city) LIKE LOWER(CONCAT('%', :destination, '%')))
          AND (:host IS NULL OR LOWER(t.planner.fullName) LIKE LOWER(CONCAT('%', :host, '%')))
          AND (:startDate IS NULL OR t.startDate >= :startDate)
          AND (:endDate IS NULL OR t.endDate <= :endDate)
    """)
    Page<Trip> searchTrips(
            @Param("destination") String destination,
            @Param("host") String host,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // ========================================
    // SEARCH TRIPS (STRICT / PRODUCTION-SAFE)
    // Only ACTIVE + NOT DELETED + PUBLISHED
    // ========================================
    @Query("""
        SELECT t FROM Trip t
        WHERE t.active = true
          AND t.deleted = false
          AND t.status = :status
          AND (:destination IS NULL OR LOWER(t.destination.city) LIKE LOWER(CONCAT('%', :destination, '%')))
          AND (:host IS NULL OR LOWER(t.planner.fullName) LIKE LOWER(CONCAT('%', :host, '%')))
          AND (:startDate IS NULL OR t.startDate >= :startDate)
          AND (:endDate IS NULL OR t.endDate <= :endDate)
    """)
    Page<Trip> searchPublishedTrips(
            @Param("destination") String destination,
            @Param("host") String host,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") TripStatus status,
            Pageable pageable
    );

    // ========================================
    // PLANNER / HOST QUERIES (OWN TRIP MANAGEMENT)
    // ========================================
    Page<Trip> findByPlannerId(Long plannerId, Pageable pageable);

    Page<Trip> findByPlannerIdAndDeletedFalse(Long plannerId, Pageable pageable);

    Page<Trip> findByPlannerIdAndDeletedTrue(Long plannerId, Pageable pageable);

    Page<Trip> findByPlannerIdAndActiveTrueAndDeletedFalse(Long plannerId, Pageable pageable);

    Page<Trip> findByPlannerIdAndActiveFalseAndDeletedFalse(Long plannerId, Pageable pageable);

    Page<Trip> findByPlannerIdAndStatusAndDeletedFalse(
            Long plannerId,
            TripStatus status,
            Pageable pageable
    );

    // ========================================
    // ADMIN MODERATION QUERIES
    // ========================================
    Page<Trip> findByDeletedFalse(Pageable pageable);

    Page<Trip> findByDeletedTrue(Pageable pageable);

    Page<Trip> findByActiveTrueAndDeletedFalse(Pageable pageable);

    Page<Trip> findByActiveFalseAndDeletedFalse(Pageable pageable);

    Page<Trip> findByStatusAndDeletedFalse(
            TripStatus status,
            Pageable pageable
    );

    // ========================================
    // LOCK TRIP FOR BOOKING (RACE CONDITION SAFE)
    // DO NOT TOUCH - critical for booking consistency
    // ========================================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id")
    Optional<Trip> findTripForUpdate(@Param("id") Long id);
}