package com.tourly.messaging.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.common.dto.ApiResponse;
import com.tourly.messaging.dto.request.SendMessageRequest;
import com.tourly.messaging.dto.request.HostReplyRequest;
import com.tourly.messaging.dto.response.ConversationResponse;
import com.tourly.messaging.dto.response.MessageCountResponse;
import com.tourly.messaging.dto.response.MessageResponse;
import com.tourly.messaging.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/messages")
@Validated
@Tag(name = "Messaging", description = "Trip-host messaging APIs for travelers and hosts")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // ========================================
    // SEND MESSAGE (Traveler)
    // ========================================
    @PostMapping
    @PreAuthorize("hasRole('TRAVELER')")
    @Operation(
            summary = "Send a message to a host",
            description = "Allows a traveler to send a message to a host for a specific trip. "
                    + "Enforces a 3-message limit per traveler-host pair across all trips.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {

        MessageResponse response = messageService.sendMessage(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", response));
    }

    // ========================================
    // REPLY MESSAGE (Host)
    // ========================================
    @PostMapping("/reply")
    @PreAuthorize("hasRole('HOST')")
    @Operation(
            summary = "Reply to a traveler message",
            description = "Allows a host to reply to a traveler's message for a specific trip. "
                    + "No message limit applies to host replies.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<MessageResponse>> replyMessage(
            @Valid @RequestBody HostReplyRequest request) {

        MessageResponse response = messageService.replyMessage(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reply sent successfully", response));
    }

    // ========================================
    // GET MESSAGES FOR TRIP (Traveler)
    // ========================================
    @GetMapping("/trip/{tripId}/host/{hostId}")
    @PreAuthorize("hasRole('TRAVELER')")
    @Operation(
            summary = "Get messages between traveler and host for a trip",
            description = "Retrieves all messages between the authenticated traveler and a host "
                    + "for a specific trip, ordered by creation timestamp ascending.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessagesForTrip(
            @PathVariable @Positive(message = "Trip ID must be greater than 0") Long tripId,
            @PathVariable @Positive(message = "Host ID must be greater than 0") Long hostId) {

        List<MessageResponse> response = messageService.getMessagesForTrip(tripId, hostId);

        return ResponseEntity.ok(ApiResponse.success("Messages fetched successfully", response));
    }

    // ========================================
    // GET HOST CONVERSATIONS (Host)
    // ========================================
    @GetMapping("/host")
    @PreAuthorize("hasRole('HOST')")
    @Operation(
            summary = "Get all conversations for host",
            description = "Retrieves all messages received by the authenticated host, "
                    + "grouped by trip and ordered by most recent message descending.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getHostConversations() {

        List<ConversationResponse> response = messageService.getHostConversations();

        return ResponseEntity.ok(ApiResponse.success("Conversations fetched successfully", response));
    }

    // ========================================
    // GET MESSAGE COUNT (Traveler)
    // ========================================
    @GetMapping("/count/host/{hostId}")
    @PreAuthorize("hasRole('TRAVELER')")
    @Operation(
            summary = "Get message count for a host",
            description = "Returns the number of messages sent by the authenticated traveler "
                    + "to the specified host, along with remaining message count and limit status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<MessageCountResponse>> getMessageCount(
            @PathVariable @Positive(message = "Host ID must be greater than 0") Long hostId) {

        MessageCountResponse response = messageService.getMessageCount(hostId);

        return ResponseEntity.ok(ApiResponse.success("Message count fetched successfully", response));
    }

    // ========================================
    // GET TRAVELER CONVERSATIONS (Traveler)
    // ========================================
    @GetMapping("/traveler")
    @PreAuthorize("hasRole('TRAVELER')")
    @Operation(
            summary = "Get all conversations for traveler",
            description = "Retrieves all messages for the authenticated traveler, "
                    + "grouped by host and trip, ordered by most recent message descending.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getTravelerConversations() {

        List<ConversationResponse> response = messageService.getTravelerConversations();

        return ResponseEntity.ok(ApiResponse.success("Conversations fetched successfully", response));
    }

    // ========================================
    // GET UNREAD MESSAGE COUNT (Any authenticated user)
    // ========================================
    @GetMapping("/unread-count")
    @Operation(
            summary = "Get unread message count",
            description = "Returns the total number of unread messages for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {

        long count = messageService.getUnreadCount();

        return ResponseEntity.ok(ApiResponse.success("Unread count fetched successfully", count));
    }
}
