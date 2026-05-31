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

        // Destination — city + state
        if (trip.getDestination() != null) {
            response.setDestination(trip.getDestination().getCity());
            response.setDestinationState(trip.getDestination().getState());
        }

        // People
        if (trip.getPlanner() != null) {
            response.setPlannerName(trip.getPlanner().getFullName());
        }
        if (trip.getHost() != null) {
            response.setHostName(trip.getHost().getFullName());
        }

        // Trip metadata — needed by host dashboard and admin
        response.setCategory(trip.getCategory());
        response.setApprovalStatus(trip.getApprovalStatus());
        response.setRejectionReason(trip.getRejectionReason());
        response.setStatus(trip.getStatus());
        response.setActive(trip.getActive());
        response.setDeleted(trip.getDeleted());
        response.setCreatedAt(trip.getCreatedAt());
        response.setUpdatedAt(trip.getUpdatedAt());
        response.setDeletedAt(trip.getDeletedAt());

        return response;
    }

    // ===============================
    // ADMIN VIEW — kept for backward
    // compatibility, delegates to base
    // ===============================
    public static TripResponse mapToAdminResponse(Trip trip) {
        return mapToResponse(trip);
    }
}