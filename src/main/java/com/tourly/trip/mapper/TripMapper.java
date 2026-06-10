package com.tourly.trip.mapper;

import com.tourly.trip.entity.*;
import com.tourly.trip.dto.response.TripResponse;
import java.util.stream.Collectors;

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

        // Rich Data Fields
        response.setMinGroupSize(trip.getMinGroupSize());
        response.setDurationDays(trip.getDurationDays());
        response.setDurationNights(trip.getDurationNights());
        response.setStartsFrom(trip.getStartsFrom());
        response.setEndsAt(trip.getEndsAt());
        response.setTripType(trip.getTripType());
        response.setDifficulty(trip.getDifficulty());
        response.setBestTime(trip.getBestTime());
        response.setBadges(trip.getBadges() != null
                ? trip.getBadges().stream().map(TripBadge::getBadgeName).collect(Collectors.toList())
                : null);
        response.setAboutDescription(trip.getAboutDescription());
        response.setMaxDiscountPercent(trip.getMaxDiscountPercent());
        response.setMaxIncreasePercent(trip.getMaxIncreasePercent());

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

        // Trip metadata
        response.setCategory(trip.getCategory());
        response.setApprovalStatus(trip.getApprovalStatus());
        response.setRejectionReason(trip.getRejectionReason());
        response.setStatus(trip.getStatus());
        response.setActive(trip.getActive());
        response.setDeleted(trip.getDeleted());
        response.setCreatedAt(trip.getCreatedAt());
        response.setUpdatedAt(trip.getUpdatedAt());
        response.setDeletedAt(trip.getDeletedAt());

        // Lists Mapping
        if (trip.getHighlights() != null) {
            response.setHighlights(trip.getHighlights().stream().map(h -> {
                TripResponse.HighlightResponse hr = new TripResponse.HighlightResponse();
                hr.setIcon(h.getIcon());
                hr.setTitle(h.getTitle());
                return hr;
            }).collect(Collectors.toList()));
        }

        if (trip.getItinerary() != null) {
            response.setItinerary(trip.getItinerary().stream().map(i -> {
                TripResponse.ItineraryDayResponse idr = new TripResponse.ItineraryDayResponse();
                idr.setDay(i.getDayNumber());
                idr.setTitle(i.getTitle());
                idr.setDescription(i.getDescription());
                idr.setStay(i.getStay());
                idr.setMeals(i.getMeals());
                return idr;
            }).collect(Collectors.toList()));
        }

        if (trip.getStays() != null) {
            response.setStays(trip.getStays().stream().map(s -> {
                TripResponse.StayResponse sr = new TripResponse.StayResponse();
                sr.setName(s.getName());
                sr.setLocation(s.getLocation());
                sr.setDescription(s.getDescription());
                if (s.getAmenities() != null) {
                    sr.setAmenities(s.getAmenities().stream().map(TripStayAmenity::getAmenity).collect(Collectors.toList()));
                }
                if (s.getImages() != null) {
                    sr.setImages(s.getImages().stream().map(TripStayImage::getImageUrl).collect(Collectors.toList()));
                }
                return sr;
            }).collect(Collectors.toList()));
        }

        if (trip.getStops() != null) {
            response.setStops(trip.getStops().stream().map(TripStop::getStopName).collect(Collectors.toList()));
        }

        if (trip.getItems() != null) {
            response.setInclusions(trip.getItems().stream()
                    .filter(i -> "INCLUSION".equalsIgnoreCase(i.getType()))
                    .map(TripItem::getDescription)
                    .collect(Collectors.toList()));
            response.setExclusions(trip.getItems().stream()
                    .filter(i -> "EXCLUSION".equalsIgnoreCase(i.getType()))
                    .map(TripItem::getDescription)
                    .collect(Collectors.toList()));
        }

        if (trip.getMedia() != null) {
            response.setGalleryUrls(trip.getMedia().stream()
                    .filter(m -> m.getIsCover() == null || !m.getIsCover())
                    .map(TripMedia::getUrl)
                    .collect(Collectors.toList()));
            trip.getMedia().stream()
                    .filter(m -> m.getIsCover() != null && m.getIsCover())
                    .findFirst()
                    .ifPresent(m -> response.setCoverImageUrl(m.getUrl()));
        }

        if (trip.getPriceBreakdown() != null) {
            response.setPriceBreakdown(trip.getPriceBreakdown().stream().map(p -> {
                TripResponse.PriceBreakdownResponse pbr = new TripResponse.PriceBreakdownResponse();
                pbr.setCategory(p.getCategory());
                pbr.setAmount(p.getAmount());
                pbr.setDescription(p.getDescription());
                return pbr;
            }).collect(Collectors.toList()));
        }

        if (trip.getBatches() != null) {
            response.setBatches(trip.getBatches().stream().map(b -> {
                TripResponse.BatchResponse br = new TripResponse.BatchResponse();
                br.setStartDate(b.getStartDate());
                br.setEndDate(b.getEndDate());
                br.setPrice(b.getPrice());
                return br;
            }).collect(Collectors.toList()));
        }

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