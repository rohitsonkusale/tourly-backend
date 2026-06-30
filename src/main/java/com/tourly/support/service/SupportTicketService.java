package com.tourly.support.service;

import com.tourly.support.dto.request.AdminReplyRequest;
import com.tourly.support.dto.request.CreateTicketRequest;
import com.tourly.support.dto.response.TicketResponse;
import com.tourly.support.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportTicketService {

    TicketResponse createTicket(CreateTicketRequest request);

    Page<TicketResponse> getMyTickets(Pageable pageable);

    TicketResponse getTicketById(Long ticketId);

    // Admin operations
    Page<TicketResponse> getAllTickets(Pageable pageable);

    Page<TicketResponse> getTicketsByStatus(TicketStatus status, Pageable pageable);

    TicketResponse replyToTicket(Long ticketId, AdminReplyRequest request);

    TicketResponse updateTicketStatus(Long ticketId, TicketStatus status);
}
