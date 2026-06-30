package com.tourly.support.controller;

import com.tourly.common.dto.ApiResponse;
import com.tourly.support.dto.request.AdminReplyRequest;
import com.tourly.support.dto.request.CreateTicketRequest;
import com.tourly.support.dto.response.TicketResponse;
import com.tourly.support.enums.TicketStatus;
import com.tourly.support.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support-tickets")
@Tag(name = "Support Tickets", description = "Support ticket management APIs")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    // =========================================
    // USER ENDPOINTS (Traveler, Host, Planner)
    // =========================================

    @PostMapping
    @PreAuthorize("hasAnyRole('TRAVELER','HOST','PLANNER','ADMIN')")
    @Operation(summary = "Create a support ticket", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = supportTicketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Support ticket created successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('TRAVELER','HOST','PLANNER','ADMIN')")
    @Operation(summary = "Get my support tickets", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getMyTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TicketResponse> tickets = supportTicketService.getMyTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success("Tickets fetched successfully", tickets));
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('TRAVELER','HOST','PLANNER','ADMIN')")
    @Operation(summary = "Get ticket by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(
            @PathVariable Long ticketId) {
        TicketResponse response = supportTicketService.getTicketById(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Ticket fetched successfully", response));
    }

    // =========================================
    // ADMIN ENDPOINTS
    // =========================================

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all support tickets (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TicketResponse> tickets = supportTicketService.getAllTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success("All tickets fetched", tickets));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get tickets by status (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getTicketsByStatus(
            @PathVariable TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TicketResponse> tickets = supportTicketService.getTicketsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tickets fetched by status", tickets));
    }

    @PutMapping("/admin/{ticketId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reply to a support ticket (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TicketResponse>> replyToTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AdminReplyRequest request) {
        TicketResponse response = supportTicketService.replyToTicket(ticketId, request);
        return ResponseEntity.ok(ApiResponse.success("Ticket replied successfully", response));
    }

    @PatchMapping("/admin/{ticketId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update ticket status (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status) {
        TicketResponse response = supportTicketService.updateTicketStatus(ticketId, status);
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated", response));
    }
}
