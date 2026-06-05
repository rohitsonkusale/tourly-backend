package com.tourly.trip.repository;

import com.tourly.trip.entity.TripItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripItineraryDayRepository extends JpaRepository<TripItineraryDay, Long> {
    void deleteByTripId(Long tripId);
}
