package com.tourly.support.service.impl;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.exception.UnauthorizedActionException;
import com.tourly.support.dto.request.AdminReplyRequest;
import com.tourly.support.dto.request.CreateTicketRequest;
import com.tourly.support.dto.response.TicketResponse;
import com.tourly.support.entity.SupportTicket;
import com.tourly.support.enums.TicketStatus;
import com.tourly.support.repository.SupportTicketRepository;
import com.tourly.support.service.SupportTicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;

    public SupportTicketServiceImpl(SupportTicketRepository ticketRepository,
                                     UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    // =========================================
    // HELPER: GET CURRENT USER
    // =========================================
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedActionException("User is not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // =========================================
    // USER OPERATIONS
    // =========================================

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        User currentUser = getCurrentUser();

        SupportTicket ticket = new SupportTicket();
        ticket.setUser(currentUser);
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());
        ticket.setStatus(TicketStatus.OPEN);

        SupportTicket saved = ticketRepository.save(ticket);
        return mapToResponse(saved, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getMyTickets(Pageable pageable) {
        User currentUser = getCurrentUser();
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(ticket -> mapToResponse(ticket, false));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long ticketId) {
        User currentUser = getCurrentUser();
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        // Users can only view their own tickets; admins can view all
        boolean isAdmin = currentUser.getRole().getName().name().equals("ADMIN");
        if (!isAdmin && !ticket.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not authorized to view this ticket");
        }

        return mapToResponse(ticket, isAdmin);
    }

    // =========================================
    // ADMIN OPERATIONS
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        return ticketRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ticket -> mapToResponse(ticket, true));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(ticket -> mapToResponse(ticket, true));
    }

    @Override
    @Transactional
    public TicketResponse replyToTicket(Long ticketId, AdminReplyRequest request) {
        User admin = getCurrentUser();

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        ticket.setAdminResponse(request.getResponse());
        ticket.setStatus(request.getStatus());
        ticket.setResolvedBy(admin);
        ticket.setResolvedAt(LocalDateTime.now());

        SupportTicket updated = ticketRepository.save(ticket);
        return mapToResponse(updated, true);
    }

    @Override
    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, TicketStatus status) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        ticket.setStatus(status);
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
            ticket.setResolvedBy(getCurrentUser());
        }

        SupportTicket updated = ticketRepository.save(ticket);
        return mapToResponse(updated, true);
    }

    // =========================================
    // MAPPER
    // =========================================
    private TicketResponse mapToResponse(SupportTicket ticket, boolean includeUserInfo) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setSubject(ticket.getSubject());
        response.setDescription(ticket.getDescription());
        response.setCategory(ticket.getCategory());
        response.setStatus(ticket.getStatus());
        response.setPriority(ticket.getPriority());
        response.setAdminResponse(ticket.getAdminResponse());
        response.setResolvedAt(ticket.getResolvedAt());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());

        if (ticket.getResolvedBy() != null) {
            response.setResolvedByName(ticket.getResolvedBy().getFullName());
        }

        if (includeUserInfo && ticket.getUser() != null) {
            response.setUserName(ticket.getUser().getFullName());
            response.setUserEmail(ticket.getUser().getEmail());
        }

        return response;
    }
}
