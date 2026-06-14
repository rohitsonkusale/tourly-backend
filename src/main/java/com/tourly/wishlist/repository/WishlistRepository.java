package com.tourly.wishlist.repository;

import com.tourly.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("SELECT w FROM Wishlist w JOIN FETCH w.trip t LEFT JOIN FETCH t.destination WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<Wishlist> findByUserIdWithTrip(@Param("userId") Long userId);

    Optional<Wishlist> findByUserIdAndTripId(Long userId, Long tripId);

    boolean existsByUserIdAndTripId(Long userId, Long tripId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId AND w.trip.id = :tripId")
    void deleteByUserIdAndTripId(@Param("userId") Long userId, @Param("tripId") Long tripId);

    long countByUserId(Long userId);
}
