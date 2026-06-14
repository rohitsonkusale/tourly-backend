package com.tourly.messaging.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class HostReplyRequest {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Traveler ID is required")
    private Long travelerId;

    @NotBlank(message = "Reply content is required")
    @Size(min = 1, max = 1000, message = "Reply content must be between 1 and 1000 characters")
    private String content;

    public HostReplyRequest() {
    }

    public HostReplyRequest(Long tripId, Long travelerId, String content) {
        this.tripId = tripId;
        this.travelerId = travelerId;
        this.content = content;
    }

    // Getters & Setters
    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getTravelerId() {
        return travelerId;
    }

    public void setTravelerId(Long travelerId) {
        this.travelerId = travelerId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
