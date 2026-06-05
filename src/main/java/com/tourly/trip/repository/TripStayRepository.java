package com.tourly.trip.repository;

import com.tourly.trip.entity.TripStay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripStayRepository extends JpaRepository<TripStay, Long> {
    void deleteByTripId(Long tripId);
}
