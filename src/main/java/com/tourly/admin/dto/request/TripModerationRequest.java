package com.tourly.admin.dto.request;

import jakarta.validation.constraints.Size;

public class TripModerationRequest {

    @Size(max = 1000, message = "Admin message cannot exceed 1000 characters")
    private String adminMessage;

    public TripModerationRequest() {}

    public String getAdminMessage() { return adminMessage; }
    public void setAdminMessage(String adminMessage) { this.adminMessage = adminMessage; }
}
