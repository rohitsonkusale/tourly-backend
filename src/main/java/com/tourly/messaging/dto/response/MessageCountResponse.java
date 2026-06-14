package com.tourly.messaging.dto.response;

public class MessageCountResponse {

    private Long hostId;
    private int messagesSent;
    private int messagesRemaining;
    private boolean limitReached;
    private boolean limitRemoved;

    public MessageCountResponse() {
    }

    public MessageCountResponse(Long hostId, int messagesSent, int messagesRemaining,
                                boolean limitReached, boolean limitRemoved) {
        this.hostId = hostId;
        this.messagesSent = messagesSent;
        this.messagesRemaining = messagesRemaining;
        this.limitReached = limitReached;
        this.limitRemoved = limitRemoved;
    }

    // Getters & Setters
    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public int getMessagesSent() {
        return messagesSent;
    }

    public void setMessagesSent(int messagesSent) {
        this.messagesSent = messagesSent;
    }

    public int getMessagesRemaining() {
        return messagesRemaining;
    }

    public void setMessagesRemaining(int messagesRemaining) {
        this.messagesRemaining = messagesRemaining;
    }

    public boolean isLimitReached() {
        return limitReached;
    }

    public void setLimitReached(boolean limitReached) {
        this.limitReached = limitReached;
    }

    public boolean isLimitRemoved() {
        return limitRemoved;
    }

    public void setLimitRemoved(boolean limitRemoved) {
        this.limitRemoved = limitRemoved;
    }
}
