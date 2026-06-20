package com.tourly.messaging.repository;

import com.tourly.auth.entity.User;
import com.tourly.messaging.entity.ContactViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContactViolationRepository extends JpaRepository<ContactViolation, Long> {

    /** Count total violations by a user (all-time) */
    long countByUser(User user);

    /** Count violations by a user within a time window (e.g., last 24 hours) */
    @Query("""
        SELECT COUNT(v) FROM ContactViolation v
        WHERE v.user = :user AND v.createdAt >= :since
    """)
    long countByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);

    /** Get recent violations for a user (for admin review) */
    List<ContactViolation> findByUserOrderByCreatedAtDesc(User user);
}
