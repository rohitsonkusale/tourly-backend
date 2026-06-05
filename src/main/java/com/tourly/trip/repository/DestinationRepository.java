package com.tourly.trip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tourly.trip.entity.Destination;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    // Find destination by city name (case-insensitive) for find-or-create
    Optional<Destination> findByCityIgnoreCase(String city);
}
