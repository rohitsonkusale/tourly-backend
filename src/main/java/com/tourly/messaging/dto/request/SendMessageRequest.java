package com.tourly.messaging.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Host ID is required")
    private Long hostId;

    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 1000, message = "Message content must be between 1 and 1000 characters")
    private String content;

    public SendMessageRequest() {
    }

    public SendMessageRequest(Long tripId, Long hostId, String content) {
        this.tripId = tripId;
        this.hostId = hostId;
        this.content = content;
    }

    // Getters & Setters
    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
