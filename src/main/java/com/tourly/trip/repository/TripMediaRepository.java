package com.tourly.trip.repository;

import com.tourly.trip.entity.TripMedia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripMediaRepository extends JpaRepository<TripMedia, Long> {
    void deleteByTripId(Long tripId);
}
