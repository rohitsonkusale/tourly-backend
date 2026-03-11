package com.tourly.trip.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.tourly.trip.entity.Trip;

import jakarta.persistence.LockModeType;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // ========================================
    // SEARCH TRIPS
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
            String destination,
            String host,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    // ========================================
    // LOCK TRIP FOR BOOKING (Race Condition Fix)
    // ========================================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id")
    Optional<Trip> findTripForUpdate(Long id);
}