package com.tourly.messaging.service;

import com.tourly.messaging.dto.request.SendMessageRequest;
import com.tourly.messaging.dto.request.HostReplyRequest;
import com.tourly.messaging.dto.response.ConversationResponse;
import com.tourly.messaging.dto.response.MessageCountResponse;
import com.tourly.messaging.dto.response.MessageResponse;

import java.util.List;

public interface MessageService {

    /**
     * Send a message from the authenticated traveler to a host for a specific trip.
     */
    MessageResponse sendMessage(SendMessageRequest request);

    /**
     * Send a reply from the authenticated host to a traveler for a specific trip.
     * No message limit applies to host replies.
     */
    MessageResponse replyMessage(HostReplyRequest request);

    /**
     * Retrieve messages between the authenticated traveler and a host for a specific trip,
     * ordered by createdAt ascending. Includes both traveler and host messages.
     */
    List<MessageResponse> getMessagesForTrip(Long tripId, Long hostId);

    /**
     * Retrieve all messages for the authenticated host, grouped by trip,
     * ordered by most recent message descending.
     * Joins with TravelerHostLink for anonymous IDs.
     */
    List<ConversationResponse> getHostConversations();

    /**
     * Return the count of messages sent by the authenticated traveler to the specified host,
     * calculate remaining messages, and check if limit is removed (link status REVEALED).
     */
    MessageCountResponse getMessageCount(Long hostId);

    /**
     * Retrieve all conversations for the authenticated traveler, grouped by host+trip,
     * ordered by most recent message descending.
     */
    List<ConversationResponse> getTravelerConversations();

    /**
     * Get the count of unread messages for the authenticated user.
     */
    long getUnreadCount();
}
