package com.tourly.trip.repository;

import com.tourly.trip.entity.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripStopRepository extends JpaRepository<TripStop, Long> {
    void deleteByTripId(Long tripId);
}
