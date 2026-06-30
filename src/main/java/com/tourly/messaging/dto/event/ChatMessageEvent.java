package com.tourly.messaging.dto.event;

import java.time.LocalDateTime;

/**
 * WebSocket event payload sent to recipients when a new message arrives.
 */
public class ChatMessageEvent {

    private Long messageId;
    private Long senderId;
    private Long recipientId;
    private Long tripId;
    private String tripTitle;
    private String content;
    private String senderType; // "TRAVELER" or "HOST"
    private boolean contactMasked;
    private LocalDateTime createdAt;

    public ChatMessageEvent() {}

    public ChatMessageEvent(Long messageId, Long senderId, Long recipientId,
                            Long tripId, String tripTitle, String content,
                            String senderType, boolean contactMasked, LocalDateTime createdAt) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.tripId = tripId;
        this.tripTitle = tripTitle;
        this.content = content;
        this.senderType = senderType;
        this.contactMasked = contactMasked;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public String getTripTitle() { return tripTitle; }
    public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public boolean isContactMasked() { return contactMasked; }
    public void setContactMasked(boolean contactMasked) { this.contactMasked = contactMasked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
