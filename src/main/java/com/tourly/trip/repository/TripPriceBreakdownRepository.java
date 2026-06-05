package com.tourly.trip.repository;

import com.tourly.trip.entity.TripPriceBreakdown;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPriceBreakdownRepository extends JpaRepository<TripPriceBreakdown, Long> {
    void deleteByTripId(Long tripId);
}
