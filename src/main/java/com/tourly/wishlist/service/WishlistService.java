package com.tourly.wishlist.service;

import com.tourly.trip.dto.response.TripResponse;

import java.util.List;

public interface WishlistService {

    void addToWishlist(Long tripId);

    void removeFromWishlist(Long tripId);

    List<TripResponse> getMyWishlist();

    boolean isInWishlist(Long tripId);

    long getWishlistCount();
}
