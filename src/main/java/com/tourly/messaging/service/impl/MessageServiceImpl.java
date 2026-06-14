package com.tourly.messaging.service.impl;

import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.MessageLimitExceededException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.exception.UnauthorizedActionException;
import com.tourly.messaging.dto.request.SendMessageRequest;
import com.tourly.messaging.dto.request.HostReplyRequest;
import com.tourly.messaging.dto.response.ConversationResponse;
import com.tourly.messaging.dto.response.MessageCountResponse;
import com.tourly.messaging.dto.response.MessageResponse;
import com.tourly.messaging.entity.Message;
import com.tourly.messaging.entity.TravelerHostLink;
import com.tourly.messaging.enums.LinkStatus;
import com.tourly.messaging.repository.MessageRepository;
import com.tourly.messaging.repository.TravelerHostLinkRepository;
import com.tourly.messaging.service.MessageService;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.entity.TripMedia;
import com.tourly.trip.repository.TripRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private static final int MESSAGE_LIMIT = 3;
    private static final String ANONYMOUS_ID_PREFIX = "Traveler_";
    private static final int ANONYMOUS_ID_LENGTH = 5;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final MessageRepository messageRepository;
    private final TravelerHostLinkRepository travelerHostLinkRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final SecureRandom secureRandom;

    public MessageServiceImpl(MessageRepository messageRepository,
                              TravelerHostLinkRepository travelerHostLinkRepository,
                              UserRepository userRepository,
                              TripRepository tripRepository) {
        this.messageRepository = messageRepository;
        this.travelerHostLinkRepository = travelerHostLinkRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.secureRandom = new SecureRandom();
    }

    // =========================================
    // SEND MESSAGE
    // =========================================
    @Override
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        User sender = getCurrentUser();

        // Validate trip exists
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Validate host exists and has HOST role
        User host = userRepository.findById(request.getHostId())
                .orElseThrow(() -> new ResourceNotFoundException("Host not found"));

        if (host.getRole() == null || host.getRole().getName() != RoleName.HOST) {
            throw new ResourceNotFoundException("Host not found");
        }

        // Check sender != recipient
        if (sender.getId().equals(host.getId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        // Validate content is not whitespace-only
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BadRequestException("Message content is required");
        }

        // Get or create the TravelerHostLink
        TravelerHostLink link = travelerHostLinkRepository.findByTravelerAndHost(sender, host)
                .orElse(null);

        // Enforce message limit (only if link status is not REVEALED)
        boolean limitRemoved = link != null && link.getStatus() == LinkStatus.REVEALED;

        if (!limitRemoved) {
            long messageCount = messageRepository.countBySenderAndRecipient(sender, host);
            if (messageCount >= MESSAGE_LIMIT) {
                throw new MessageLimitExceededException(
                        "Message limit reached. Select a planner or purchase a service to continue messaging.");
            }
        }

        // Create the link if it doesn't exist yet
        if (link == null) {
            link = new TravelerHostLink();
            link.setTraveler(sender);
            link.setHost(host);
            link.setAnonymousId(generateAnonymousId());
            link.setStatus(LinkStatus.ANONYMOUS);
            travelerHostLinkRepository.save(link);
        }

        // Persist the message
        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(host);
        message.setTrip(trip);
        message.setContent(request.getContent().trim());
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        return mapToMessageResponse(savedMessage, "TRAVELER");
    }

    // =========================================
    // HOST REPLY MESSAGE
    // =========================================
    @Override
    @Transactional
    public MessageResponse replyMessage(HostReplyRequest request) {
        User host = getCurrentUser();

        // Validate trip exists
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Validate traveler exists
        User traveler = userRepository.findById(request.getTravelerId())
                .orElseThrow(() -> new ResourceNotFoundException("Traveler not found"));

        // Check host != traveler
        if (host.getId().equals(traveler.getId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        // Validate content is not whitespace-only
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BadRequestException("Reply content is required");
        }

        // Persist the reply (host → traveler)
        Message message = new Message();
        message.setSender(host);
        message.setRecipient(traveler);
        message.setTrip(trip);
        message.setContent(request.getContent().trim());
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        return mapToMessageResponse(savedMessage, "HOST");
    }

    // =========================================
    // GET MESSAGES FOR TRIP
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesForTrip(Long tripId, Long hostId) {
        User traveler = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Host not found"));

        // Fetch all messages in both directions between traveler and host for this trip
        List<Message> messages = messageRepository.findConversationByTripAndUsers(trip, traveler, host);

        return messages.stream()
                .map(m -> {
                    String senderType = m.getSender().getId().equals(traveler.getId()) ? "TRAVELER" : "HOST";
                    return mapToMessageResponse(m, senderType);
                })
                .collect(Collectors.toList());
    }

    // =========================================
    // GET HOST CONVERSATIONS
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getHostConversations() {
        User host = getCurrentUser();

        // Get all messages where this host is sender OR recipient
        List<Message> allMessages = messageRepository.findAllByHost(host);

        if (allMessages.isEmpty()) {
            return Collections.emptyList();
        }

        // Group messages by (traveler, trip) combination for conversations
        // Determine the traveler: if host is sender, traveler is recipient; otherwise sender
        Map<String, List<Message>> groupedMessages = new LinkedHashMap<>();
        for (Message msg : allMessages) {
            User traveler = msg.getSender().getId().equals(host.getId())
                    ? msg.getRecipient()
                    : msg.getSender();
            String key = traveler.getId() + "_" + msg.getTrip().getId();
            groupedMessages.computeIfAbsent(key, k -> new ArrayList<>()).add(msg);
        }

        // Build ConversationResponse for each group
        List<ConversationResponse> conversations = new ArrayList<>();

        for (Map.Entry<String, List<Message>> entry : groupedMessages.entrySet()) {
            List<Message> msgs = entry.getValue();
            Message firstMsg = msgs.get(0); // Most recent (DESC order)
            User traveler = firstMsg.getSender().getId().equals(host.getId())
                    ? firstMsg.getRecipient()
                    : firstMsg.getSender();
            Trip trip = firstMsg.getTrip();

            // Get the TravelerHostLink for anonymous ID
            Optional<TravelerHostLink> linkOpt = travelerHostLinkRepository
                    .findByTravelerAndHost(traveler, host);

            String anonymousId = linkOpt.map(TravelerHostLink::getAnonymousId).orElse("Unknown");
            boolean revealed = linkOpt.map(l -> l.getStatus() == LinkStatus.REVEALED).orElse(false);
            String travelerName = revealed ? traveler.getFullName() : null;

            // Get trip cover image
            String tripImage = getCoverImage(trip);

            // Sort messages within group by createdAt ascending for display
            List<Message> sortedMsgs = msgs.stream()
                    .sorted(Comparator.comparing(Message::getCreatedAt))
                    .collect(Collectors.toList());

            List<MessageResponse> messageResponses = sortedMsgs.stream()
                    .map(m -> {
                        String senderType = m.getSender().getId().equals(host.getId()) ? "HOST" : "TRAVELER";
                        return mapToMessageResponse(m, senderType);
                    })
                    .collect(Collectors.toList());

            // Check if any message is unread
            boolean hasUnread = msgs.stream().anyMatch(m -> !Boolean.TRUE.equals(m.getIsRead()));

            // Last message timestamp (most recent)
            LocalDateTime lastMessageAt = firstMsg.getCreatedAt();

            ConversationResponse conversation = new ConversationResponse(
                    trip.getId(),
                    trip.getTitle(),
                    tripImage,
                    anonymousId,
                    travelerName,
                    revealed,
                    messageResponses,
                    lastMessageAt,
                    hasUnread
            );

            conversations.add(conversation);
        }

        // Sort by most recent message descending (already in order from query,
        // but re-sort to be safe after grouping)
        conversations.sort((c1, c2) -> c2.getLastMessageAt().compareTo(c1.getLastMessageAt()));

        return conversations;
    }

    // =========================================
    // GET MESSAGE COUNT
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public MessageCountResponse getMessageCount(Long hostId) {
        User traveler = getCurrentUser();

        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Host not found"));

        long count = messageRepository.countBySenderAndRecipient(traveler, host);
        int messagesSent = (int) count;

        // Check if limit is removed (link status REVEALED)
        Optional<TravelerHostLink> linkOpt = travelerHostLinkRepository.findByTravelerAndHost(traveler, host);
        boolean limitRemoved = linkOpt.map(l -> l.getStatus() == LinkStatus.REVEALED).orElse(false);

        int messagesRemaining;
        boolean limitReached;

        if (limitRemoved) {
            // Unlimited messaging
            messagesRemaining = Integer.MAX_VALUE;
            limitReached = false;
        } else {
            messagesRemaining = Math.max(0, MESSAGE_LIMIT - messagesSent);
            limitReached = messagesSent >= MESSAGE_LIMIT;
        }

        return new MessageCountResponse(
                hostId,
                messagesSent,
                messagesRemaining,
                limitReached,
                limitRemoved
        );
    }

    // =========================================
    // GET TRAVELER CONVERSATIONS
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getTravelerConversations() {
        User traveler = getCurrentUser();

        // Get all messages where this traveler is sender OR recipient
        List<Message> allMessages = messageRepository.findAllByTraveler(traveler);

        if (allMessages.isEmpty()) {
            return Collections.emptyList();
        }

        // Group messages by (host, trip) combination
        Map<String, List<Message>> groupedMessages = new LinkedHashMap<>();
        for (Message msg : allMessages) {
            User host = msg.getSender().getId().equals(traveler.getId())
                    ? msg.getRecipient()
                    : msg.getSender();
            String key = host.getId() + "_" + msg.getTrip().getId();
            groupedMessages.computeIfAbsent(key, k -> new ArrayList<>()).add(msg);
        }

        List<ConversationResponse> conversations = new ArrayList<>();

        for (Map.Entry<String, List<Message>> entry : groupedMessages.entrySet()) {
            List<Message> msgs = entry.getValue();
            Message firstMsg = msgs.get(0); // Most recent (DESC order)
            User host = firstMsg.getSender().getId().equals(traveler.getId())
                    ? firstMsg.getRecipient()
                    : firstMsg.getSender();
            Trip trip = firstMsg.getTrip();

            String tripImage = getCoverImage(trip);

            // Sort messages by createdAt ascending for display
            List<Message> sortedMsgs = msgs.stream()
                    .sorted(Comparator.comparing(Message::getCreatedAt))
                    .collect(Collectors.toList());

            List<MessageResponse> messageResponses = sortedMsgs.stream()
                    .map(m -> {
                        String senderType = m.getSender().getId().equals(traveler.getId()) ? "TRAVELER" : "HOST";
                        return mapToMessageResponse(m, senderType);
                    })
                    .collect(Collectors.toList());

            // Check if any message from host is unread
            boolean hasUnread = msgs.stream()
                    .anyMatch(m -> !m.getSender().getId().equals(traveler.getId())
                            && !Boolean.TRUE.equals(m.getIsRead()));

            LocalDateTime lastMessageAt = firstMsg.getCreatedAt();

            // For traveler view: show host name, use hostId as anonymousTravelerId field (repurposed)
            ConversationResponse conversation = new ConversationResponse(
                    trip.getId(),
                    trip.getTitle(),
                    tripImage,
                    String.valueOf(host.getId()), // using anonymousTravelerId field to pass hostId
                    host.getFullName(), // travelerName field used as hostName
                    true, // revealed = true (traveler always sees host name)
                    messageResponses,
                    lastMessageAt,
                    hasUnread
            );

            conversations.add(conversation);
        }

        conversations.sort((c1, c2) -> c2.getLastMessageAt().compareTo(c1.getLastMessageAt()));

        return conversations;
    }

    // =========================================
    // PRIVATE HELPERS
    // =========================================

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new UnauthorizedActionException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String generateAnonymousId() {
        StringBuilder sb = new StringBuilder(ANONYMOUS_ID_PREFIX);
        for (int i = 0; i < ANONYMOUS_ID_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(secureRandom.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    private MessageResponse mapToMessageResponse(Message message, String senderType) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getRecipient().getId(),
                message.getTrip().getId(),
                message.getContent(),
                message.getCreatedAt(),
                senderType
        );
    }

    private String getCoverImage(Trip trip) {
        if (trip.getMedia() == null || trip.getMedia().isEmpty()) {
            return null;
        }
        return trip.getMedia().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsCover()))
                .findFirst()
                .map(TripMedia::getUrl)
                .orElse(trip.getMedia().get(0).getUrl());
    }
}
