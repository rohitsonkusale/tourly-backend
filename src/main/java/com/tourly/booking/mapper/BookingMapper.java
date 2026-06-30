package com.tourly.booking.mapper;

import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.entity.Booking;
import com.tourly.trip.entity.Destination;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.entity.TripMedia;
import com.tourly.auth.entity.User;

public class BookingMapper {

    public static BookingResponse toResponse(Booking booking) {

        BookingResponse response = new BookingResponse();

        response.setBookingId(booking.getId());
        response.setBookingRef(booking.getBookingRef());
        response.setSeatsBooked(booking.getSeatsBooked());
        response.setTotalPrice(booking.getTotalPrice());
        response.setAmountPaid(booking.getAmountPaid());
        response.setCreatedAt(booking.getCreatedAt());

        if (booking.getStatus() != null) {
            response.setBookingStatus(booking.getStatus().name());
        }
        if (booking.getPaymentStatus() != null) {
            response.setPaymentStatus(booking.getPaymentStatus().name());
        }

        // Trip details
        Trip trip = booking.getTrip();
        if (trip != null) {
            response.setTripId(trip.getId());
            response.setTripTitle(trip.getTitle());
            response.setTripStartDate(trip.getStartDate());
            response.setTripEndDate(trip.getEndDate());
            response.setStartsFrom(trip.getStartsFrom());
            response.setDurationDays(trip.getDurationDays());
            response.setDurationNights(trip.getDurationNights());

            // Destination
            Destination dest = trip.getDestination();
            if (dest != null) {
                String city = dest.getCity();
                String state = dest.getState();
                response.setDestination(state != null ? city + ", " + state : city);
            }

            // Cover image
            if (trip.getMedia() != null && !trip.getMedia().isEmpty()) {
                String coverUrl = trip.getMedia().stream()
                        .filter(m -> Boolean.TRUE.equals(m.getIsCover()))
                        .map(TripMedia::getUrl)
                        .findFirst()
                        .orElse(trip.getMedia().get(0).getUrl());
                response.setTripCoverImage(coverUrl);
            }

            // Host info (planner or host)
            User host = trip.getPlanner() != null ? trip.getPlanner() : trip.getHost();
            if (host != null) {
                response.setHostId(host.getId());
                response.setHostName(host.getFullName());
                response.setHostAvatar(host.getAvatar());
            }

            // Planner info
            if (trip.getPlanner() != null) {
                response.setPlannerId(trip.getPlanner().getId());
                response.setPlannerName(trip.getPlanner().getFullName());
            }
        }

        // Traveler info
        User traveler = booking.getTraveler();
        if (traveler != null) {
            response.setTravelerId(traveler.getId());
            response.setTravelerName(traveler.getFullName());
        }

        return response;
    }
}