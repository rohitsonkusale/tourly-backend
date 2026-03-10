package com.tourly.trip.mapper;

import com.tourly.trip.entity.Trip;
import com.tourly.trip.dto.response.TripResponse;

public class TripMapper {

    public static TripResponse mapToResponse(Trip trip) {

        TripResponse response = new TripResponse();

        response.setId(trip.getId());
        response.setTitle(trip.getTitle());
        response.setDescription(trip.getDescription());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setBasePrice(trip.getBasePrice());
        response.setTotalSeats(trip.getTotalSeats());
        response.setBookedSeats(trip.getBookedSeats());

        if (trip.getDestination() != null) {
            response.setDestination(trip.getDestination().getCity());
        }

        if (trip.getPlanner() != null) {
            response.setPlannerName(trip.getPlanner().getFullName());
        }

        return response;
    }
}