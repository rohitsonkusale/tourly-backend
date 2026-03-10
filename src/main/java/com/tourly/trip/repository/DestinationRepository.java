package com.tourly.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tourly.trip.entity.Destination;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

}