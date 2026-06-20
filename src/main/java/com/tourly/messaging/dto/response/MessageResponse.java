package com.tourly.messaging.dto.response;

import java.time.LocalDateTime;

public class MessageResponse {

    private Long id;
    private Long senderId;
    private Long recipientId;
    private Long tripId;
    private String content;
    private LocalDateTime createdAt;
    private String senderType; // "TRAVELER" or "HOST"
    private boolean contactMasked;

    public MessageResponse() {
    }

    public MessageResponse(Long id, Long senderId, Long recipientId, Long tripId,
                           String content, LocalDateTime createdAt, String senderType) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.tripId = tripId;
        this.content = content;
        this.createdAt = createdAt;
        this.senderType = senderType;
        this.contactMasked = false;
    }

    public MessageResponse(Long id, Long senderId, Long recipientId, Long tripId,
                           String content, LocalDateTime createdAt, String senderType,
                           boolean contactMasked) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.tripId = tripId;
        this.content = content;
        this.createdAt = createdAt;
        this.senderType = senderType;
        this.contactMasked = contactMasked;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public boolean isContactMasked() {
        return contactMasked;
    }

    public void setContactMasked(boolean contactMasked) {
        this.contactMasked = contactMasked;
    }
}
