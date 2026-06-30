package com.tourly.support.dto.request;

import com.tourly.support.enums.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminReplyRequest {

    @NotBlank(message = "Response is required")
    @Size(max = 5000, message = "Response cannot exceed 5000 characters")
    private String response;

    private TicketStatus status = TicketStatus.RESOLVED;

    // Getters & Setters
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
}
