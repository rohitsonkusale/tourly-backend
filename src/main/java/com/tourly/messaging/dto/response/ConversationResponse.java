package com.tourly.messaging.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationResponse {

    private Long tripId;
    private String tripTitle;
    private String tripImage;
    private String anonymousTravelerId;
    private String travelerName;
    private boolean revealed;
    private List<MessageResponse> messages;
    private LocalDateTime lastMessageAt;
    private boolean hasUnread;

    public ConversationResponse() {
    }

    public ConversationResponse(Long tripId, String tripTitle, String tripImage,
                                String anonymousTravelerId, String travelerName,
                                boolean revealed, List<MessageResponse> messages,
                                LocalDateTime lastMessageAt, boolean hasUnread) {
        this.tripId = tripId;
        this.tripTitle = tripTitle;
        this.tripImage = tripImage;
        this.anonymousTravelerId = anonymousTravelerId;
        this.travelerName = travelerName;
        this.revealed = revealed;
        this.messages = messages;
        this.lastMessageAt = lastMessageAt;
        this.hasUnread = hasUnread;
    }

    // Getters & Setters
    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public void setTripTitle(String tripTitle) {
        this.tripTitle = tripTitle;
    }

    public String getTripImage() {
        return tripImage;
    }

    public void setTripImage(String tripImage) {
        this.tripImage = tripImage;
    }

    public String getAnonymousTravelerId() {
        return anonymousTravelerId;
    }

    public void setAnonymousTravelerId(String anonymousTravelerId) {
        this.anonymousTravelerId = anonymousTravelerId;
    }

    public String getTravelerName() {
        return travelerName;
    }

    public void setTravelerName(String travelerName) {
        this.travelerName = travelerName;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public List<MessageResponse> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageResponse> messages) {
        this.messages = messages;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public boolean isHasUnread() {
        return hasUnread;
    }

    public void setHasUnread(boolean hasUnread) {
        this.hasUnread = hasUnread;
    }
}
