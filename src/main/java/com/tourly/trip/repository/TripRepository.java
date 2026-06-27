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
import com.tourly.trip.enums.TripCategory;
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
    // ADVANCED SEARCH (FULL FILTER SUPPORT)
    // Supports price range, category, difficulty,
    // tripType, startsFrom, and seats-available
    // ========================================
    @Query("""
        SELECT t FROM Trip t
        LEFT JOIN t.destination d
        LEFT JOIN t.planner p
        WHERE t.active = true
          AND t.deleted = false
          AND t.status = :status
          AND (:destination IS NULL OR LOWER(d.city) LIKE LOWER(CONCAT('%', :destination, '%')))
          AND (:host IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :host, '%')))
          AND (:startDate IS NULL OR t.startDate >= :startDate)
          AND (:endDate IS NULL OR t.endDate <= :endDate)
          AND (:priceMin IS NULL OR t.basePrice >= :priceMin)
          AND (:priceMax IS NULL OR t.basePrice <= :priceMax)
          AND (:category IS NULL OR t.category = :category)
          AND (:difficulty IS NULL OR LOWER(t.difficulty) = LOWER(:difficulty))
          AND (:tripType IS NULL OR LOWER(t.tripType) LIKE LOWER(CONCAT('%', :tripType, '%')))
          AND (:startsFrom IS NULL OR LOWER(t.startsFrom) LIKE LOWER(CONCAT('%', :startsFrom, '%')))
          AND (:seatsAvailable = false OR (t.totalSeats - t.bookedSeats) > 0)
    """)
    Page<Trip> searchTripsAdvanced(
            @Param("destination") String destination,
            @Param("host") String host,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("priceMin") java.math.BigDecimal priceMin,
            @Param("priceMax") java.math.BigDecimal priceMax,
            @Param("category") TripCategory category,
            @Param("difficulty") String difficulty,
            @Param("tripType") String tripType,
            @Param("startsFrom") String startsFrom,
            @Param("seatsAvailable") boolean seatsAvailable,
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

    long countByPlannerIdAndActiveTrueAndDeletedFalseAndStartDateAfter(Long plannerId, LocalDate date);

    long countByPlannerIdAndActiveTrueAndDeletedFalseAndStatus(Long plannerId, TripStatus status);

    // HOST-specific queries
    Page<Trip> findByHostId(Long hostId, Pageable pageable);

    Page<Trip> findByHostIdAndDeletedFalse(Long hostId, Pageable pageable);

    Page<Trip> findByHostIdAndDeletedTrue(Long hostId, Pageable pageable);

    Page<Trip> findByHostIdAndActiveTrueAndDeletedFalse(Long hostId, Pageable pageable);

    Page<Trip> findByHostIdAndActiveFalseAndDeletedFalse(Long hostId, Pageable pageable);

    Page<Trip> findByHostIdAndStatusAndDeletedFalse(
            Long hostId,
            TripStatus status,
            Pageable pageable
    );

    long countByHostIdAndActiveTrueAndDeletedFalseAndStartDateAfter(Long hostId, LocalDate date);

    long countByHostIdAndActiveTrueAndDeletedFalseAndStatus(Long hostId, TripStatus status);

    // Combined queries (host OR planner)
    @Query("SELECT t FROM Trip t WHERE (t.host.id = :userId OR t.planner.id = :userId)")
    Page<Trip> findByHostIdOrPlannerId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE (t.host.id = :userId OR t.planner.id = :userId) AND t.deleted = false")
    Page<Trip> findByHostIdOrPlannerIdAndDeletedFalse(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE (t.host.id = :userId OR t.planner.id = :userId) AND t.deleted = true")
    Page<Trip> findByHostIdOrPlannerIdAndDeletedTrue(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE (t.host.id = :userId OR t.planner.id = :userId) AND t.active = true AND t.deleted = false")
    Page<Trip> findByHostIdOrPlannerIdAndActiveTrueAndDeletedFalse(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE (t.host.id = :userId OR t.planner.id = :userId) AND t.active = false AND t.deleted = false")
    Page<Trip> findByHostIdOrPlannerIdAndActiveFalseAndDeletedFalse(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE (t.host.id = :userId OR t.planner.id = :userId) AND t.status = :status AND t.deleted = false")
    Page<Trip> findByHostIdOrPlannerIdAndStatusAndDeletedFalse(@Param("userId") Long userId, @Param("status") TripStatus status, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Trip t WHERE (t.host.id = :userId OR t.planner.id = :userId) AND t.active = true AND t.deleted = false AND t.startDate > :date")
    long countByHostIdOrPlannerIdAndActiveTrueAndDeletedFalseAndStartDateAfter(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Trip t WHERE (t.host.id = :userId OR t.planner.id = :userId) AND t.active = true AND t.deleted = false AND t.status = :status")
    long countByHostIdOrPlannerIdAndActiveTrueAndDeletedFalseAndStatus(@Param("userId") Long userId, @Param("status") TripStatus status);

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

    // Trips submitted by hosts awaiting admin approval
    @Query("SELECT t FROM Trip t LEFT JOIN FETCH t.host LEFT JOIN FETCH t.planner LEFT JOIN FETCH t.destination WHERE t.approvalStatus = :approvalStatus AND t.deleted = false")
    java.util.List<Trip> findByApprovalStatusAndDeletedFalse(
            @Param("approvalStatus") com.tourly.trip.enums.ApprovalStatus approvalStatus
    );

    // ========================================
    // LOCK TRIP FOR BOOKING (RACE CONDITION SAFE)
    // DO NOT TOUCH - critical for booking consistency
    // ========================================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id")
    Optional<Trip> findTripForUpdate(@Param("id") Long id);

    // ========================================
    // ADMIN DASHBOARD STATS
    // ========================================
    long countByDeletedFalse();
}