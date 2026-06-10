package com.tourly.trip.repository;

import com.tourly.trip.entity.TripEditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripEditLogRepository extends JpaRepository<TripEditLog, Long> {

    /** Get all edit logs for a trip, ordered by most recent first */
    List<TripEditLog> findByTripIdOrderByCreatedAtDesc(Long tripId);

    /** Get the latest edit session logs (most recent edit only) */
    @Query("SELECT l FROM TripEditLog l WHERE l.trip.id = :tripId AND l.editSessionId = " +
           "(SELECT l2.editSessionId FROM TripEditLog l2 WHERE l2.trip.id = :tripId ORDER BY l2.createdAt DESC LIMIT 1)")
    List<TripEditLog> findLatestEditSession(@Param("tripId") Long tripId);

    /** Count distinct edit sessions for a trip (= total number of times the trip was edited) */
    @Query("SELECT COUNT(DISTINCT l.editSessionId) FROM TripEditLog l WHERE l.trip.id = :tripId")
    int countEditSessions(@Param("tripId") Long tripId);
}
