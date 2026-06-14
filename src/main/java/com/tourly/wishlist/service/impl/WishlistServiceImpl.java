package com.tourly.wishlist.service.impl;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.auth.security.UserPrincipal;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.mapper.TripMapper;
import com.tourly.trip.repository.TripRepository;
import com.tourly.wishlist.entity.Wishlist;
import com.tourly.wishlist.repository.WishlistRepository;
import com.tourly.wishlist.service.WishlistService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               UserRepository userRepository,
                               TripRepository tripRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
    }

    private Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getId();
    }

    @Override
    @Transactional
    public void addToWishlist(Long tripId) {
        Long userId = getCurrentUserId();

        if (wishlistRepository.existsByUserIdAndTripId(userId, tripId)) {
            return; // already in wishlist, idempotent
        }

        // Validate trip exists
        if (!tripRepository.existsById(tripId)) {
            throw new RuntimeException("Trip not found with id: " + tripId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.getReferenceById(tripId);

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setTrip(trip);
        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long tripId) {
        Long userId = getCurrentUserId();
        wishlistRepository.deleteByUserIdAndTripId(userId, tripId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getMyWishlist() {
        Long userId = getCurrentUserId();
        List<Wishlist> wishlists = wishlistRepository.findByUserIdWithTrip(userId);

        List<TripResponse> responses = new ArrayList<>();
        for (Wishlist w : wishlists) {
            Trip trip = w.getTrip();
            // Re-fetch from repository to ensure all lazy collections are accessible
            Trip fullTrip = tripRepository.findById(trip.getId()).orElse(null);
            if (fullTrip != null) {
                responses.add(TripMapper.mapToResponse(fullTrip));
            }
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long tripId) {
        Long userId = getCurrentUserId();
        return wishlistRepository.existsByUserIdAndTripId(userId, tripId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getWishlistCount() {
        Long userId = getCurrentUserId();
        return wishlistRepository.countByUserId(userId);
    }
}
