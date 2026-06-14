package com.tourly.messaging.repository;

import com.tourly.auth.entity.User;
import com.tourly.messaging.entity.TravelerHostLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelerHostLinkRepository extends JpaRepository<TravelerHostLink, Long> {

    // =====================================
    // FIND: Link between a specific traveler and host
    // =====================================
    Optional<TravelerHostLink> findByTravelerAndHost(User traveler, User host);

    // =====================================
    // EXISTS: Check if a link already exists for a traveler-host pair
    // =====================================
    boolean existsByTravelerAndHost(User traveler, User host);
}
