package com.tourly.trip.repository;

import com.tourly.trip.entity.TripHighlight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripHighlightRepository extends JpaRepository<TripHighlight, Long> {
    void deleteByTripId(Long tripId);
}
