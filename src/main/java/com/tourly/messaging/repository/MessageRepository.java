package com.tourly.messaging.repository;

import com.tourly.auth.entity.User;
import com.tourly.messaging.entity.Message;
import com.tourly.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // =====================================
    // TRAVELER: Messages for a specific trip-host conversation
    // =====================================
    List<Message> findBySenderAndRecipientAndTripOrderByCreatedAtAsc(User sender, User recipient, Trip trip);

    // =====================================
    // COUNT: Messages sent from sender to recipient (across all trips)
    // =====================================
    long countBySenderAndRecipient(User sender, User recipient);

    // =====================================
    // HOST: All messages received, grouped by trip (ordered by most recent first)
    // =====================================
    @Query("""
        SELECT m FROM Message m
        WHERE m.recipient = :recipient
        ORDER BY m.createdAt DESC
    """)
    List<Message> findByRecipientOrderByCreatedAtDesc(@Param("recipient") User recipient);

    // =====================================
    // HOST: Find messages by recipient grouped by trip
    // =====================================
    @Query("""
        SELECT m FROM Message m
        WHERE m.recipient = :recipient
        ORDER BY m.trip.id, m.createdAt ASC
    """)
    List<Message> findByRecipientGroupedByTrip(@Param("recipient") User recipient);

    // =====================================
    // BIDIRECTIONAL: All messages between two users for a specific trip
    // =====================================
    @Query("""
        SELECT m FROM Message m
        WHERE m.trip = :trip
          AND ((m.sender = :user1 AND m.recipient = :user2)
               OR (m.sender = :user2 AND m.recipient = :user1))
        ORDER BY m.createdAt ASC
    """)
    List<Message> findConversationByTripAndUsers(
            @Param("trip") Trip trip,
            @Param("user1") User user1,
            @Param("user2") User user2);

    // =====================================
    // HOST: All messages in both directions for a host (sent or received)
    // =====================================
    @Query("""
        SELECT m FROM Message m
        WHERE m.sender = :host OR m.recipient = :host
        ORDER BY m.createdAt DESC
    """)
    List<Message> findAllByHost(@Param("host") User host);

    // =====================================
    // TRAVELER: All messages in both directions for a traveler (sent or received)
    // =====================================
    @Query("""
        SELECT m FROM Message m
        WHERE m.sender = :traveler OR m.recipient = :traveler
        ORDER BY m.createdAt DESC
    """)
    List<Message> findAllByTraveler(@Param("traveler") User traveler);

    // =====================================
    // COUNT: Unread messages received by a user
    // =====================================
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.recipient = :user AND m.isRead = false
    """)
    long countUnreadByRecipient(@Param("user") User user);
}
