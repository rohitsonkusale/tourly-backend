package com.tourly.trip.repository;

import com.tourly.trip.entity.TripItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripItemRepository extends JpaRepository<TripItem, Long> {
    void deleteByTripId(Long tripId);
}
