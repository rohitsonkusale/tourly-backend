package com.tourly.trip.repository;

import com.tourly.trip.entity.TripBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripBatchRepository extends JpaRepository<TripBatch, Long> {
    void deleteByTripId(Long tripId);
}
