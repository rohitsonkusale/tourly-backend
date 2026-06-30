package com.tourly.messaging.service;

import com.tourly.messaging.dto.event.ChatMessageEvent;
import com.tourly.messaging.entity.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service responsible for broadcasting chat events via WebSocket.
 * Sends real-time message notifications to specific users.
 */
@Service
public class ChatEventService {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatEventService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast a new message event to both the sender and recipient.
     * Each user subscribes to /user/queue/messages.
     *
     * @param message    The saved message entity
     * @param senderType "TRAVELER" or "HOST"
     */
    public void broadcastMessage(Message message, String senderType) {
        ChatMessageEvent event = new ChatMessageEvent(
                message.getId(),
                message.getSender().getId(),
                message.getRecipient().getId(),
                message.getTrip().getId(),
                message.getTrip().getTitle(),
                message.getContent(),
                senderType,
                Boolean.TRUE.equals(message.getContactMasked()),
                message.getCreatedAt()
        );

        // Send to recipient — they see a new incoming message
        String recipientEmail = message.getRecipient().getEmail();
        messagingTemplate.convertAndSendToUser(
                recipientEmail,
                "/queue/messages",
                event
        );

        // Also send to sender — confirms message delivery (for multi-device/tab sync)
        String senderEmail = message.getSender().getEmail();
        messagingTemplate.convertAndSendToUser(
                senderEmail,
                "/queue/messages",
                event
        );
    }
}
