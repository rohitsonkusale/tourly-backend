package com.tourly.admin.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.dto.ApiResponse;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.exception.UnauthorizedActionException;
import com.tourly.payment.entity.Refund;
import com.tourly.payment.enums.RefundStatus;
import com.tourly.payment.repository.RefundRepository;
import com.tourly.payment.service.RefundService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/refunds")
@Validated
@Tag(name = "Admin Refund Management", description = "Admin APIs for reviewing and processing refund requests")
public class AdminRefundController {

    private final RefundRepository refundRepository;
    private final RefundService refundService;
    private final UserRepository userRepository;

    public AdminRefundController(RefundRepository refundRepository,
                                  RefundService refundService,
                                  UserRepository userRepository) {
        this.refundRepository = refundRepository;
        this.refundService = refundService;
        this.userRepository = userRepository;
    }

    // =========================================
    // GET ALL REFUND REQUESTS
    // =========================================
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Get all refund requests", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<AdminRefundResponse>>> getAllRefunds(
            @RequestParam(required = false) String status) {

        List<Refund> refunds;
        if (status != null && !status.isBlank()) {
            RefundStatus refundStatus = RefundStatus.valueOf(status.toUpperCase());
            refunds = refundRepository.findByStatusOrderByRequestedAtDesc(refundStatus);
        } else {
            refunds = refundRepository.findAllByOrderByRequestedAtDesc();
        }

        List<AdminRefundResponse> responses = refunds.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Refund requests fetched successfully", responses));
    }

    // =========================================
    // GET SINGLE REFUND DETAIL
    // =========================================
    @GetMapping("/{refundId}")
    @Transactional(readOnly = true)
    @Operation(summary = "Get refund request detail", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AdminRefundResponse>> getRefundDetail(@PathVariable Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        return ResponseEntity.ok(ApiResponse.success("Refund detail fetched", mapToResponse(refund)));
    }

    // =========================================
    // APPROVE REFUND (after manual payment)
    // =========================================
    @PutMapping("/{refundId}/approve")
    @Operation(summary = "Approve refund after manual bank transfer", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> approveRefund(
            @PathVariable Long refundId,
            @RequestBody ApproveRefundRequest request,
            Authentication authentication) {

        Long adminUserId = getAdminUserId(authentication);
        refundService.approveAndProcessRefund(refundId, adminUserId,
                request.getTransactionReference(),
                request.getPaymentMethod(),
                request.getAdminNotes());

        return ResponseEntity.ok(ApiResponse.success("Refund marked as completed"));
    }

    // =========================================
    // REJECT REFUND
    // =========================================
    @PutMapping("/{refundId}/reject")
    @Operation(summary = "Reject a pending refund request", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> rejectRefund(
            @PathVariable Long refundId,
            @RequestBody RejectRefundRequest request,
            Authentication authentication) {

        Long adminUserId = getAdminUserId(authentication);
        refundService.rejectRefund(refundId, adminUserId, request.getReason());

        return ResponseEntity.ok(ApiResponse.success("Refund rejected"));
    }

    // =========================================
    // HELPERS
    // =========================================
    private Long getAdminUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedActionException("Admin not authenticated");
        }
        User admin = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        return admin.getId();
    }

    private AdminRefundResponse mapToResponse(Refund refund) {
        AdminRefundResponse r = new AdminRefundResponse();
        r.setRefundId(refund.getId());
        r.setBookingId(refund.getBooking() != null ? refund.getBooking().getId() : null);
        r.setBookingRef(refund.getBooking() != null ? refund.getBooking().getBookingRef() : null);
        r.setTripTitle(refund.getBooking() != null && refund.getBooking().getTrip() != null
                ? refund.getBooking().getTrip().getTitle() : null);

        // Traveler info
        if (refund.getBooking() != null && refund.getBooking().getTraveler() != null) {
            User traveler = refund.getBooking().getTraveler();
            r.setTravelerName(traveler.getFullName());
            r.setTravelerEmail(traveler.getEmail());
            r.setTravelerPhone(traveler.getPhone());
        }

        r.setOriginalAmount(refund.getOriginalAmount());
        r.setRefundAmount(refund.getRefundAmount());
        r.setRefundType(refund.getRefundType() != null ? refund.getRefundType().name() : null);
        r.setStatus(refund.getStatus() != null ? refund.getStatus().name() : null);
        r.setReason(refund.getReason());
        r.setAdminNotes(refund.getAdminNotes());
        r.setRequestedAt(refund.getRequestedAt() != null ? refund.getRequestedAt().toString() : null);
        r.setProcessedAt(refund.getProcessedAt() != null ? refund.getProcessedAt().toString() : null);
        r.setRazorpayRefundId(refund.getRazorpayRefundId());

        // Bank details
        r.setAccountHolderName(refund.getAccountHolderName());
        r.setAccountNumber(refund.getAccountNumber());
        r.setIfscCode(refund.getIfscCode());
        r.setBankName(refund.getBankName());

        // Payment proof (filled after admin completes manual transfer)
        r.setTransactionReference(refund.getTransactionReference());
        r.setPaymentMethod(refund.getPaymentMethod());
        r.setPaidOn(refund.getPaidOn() != null ? refund.getPaidOn().toString() : null);

        // Processed by
        if (refund.getProcessedBy() != null) {
            r.setProcessedByName(refund.getProcessedBy().getFullName());
        }

        return r;
    }

    // =========================================
    // RESPONSE DTO
    // =========================================
    public static class AdminRefundResponse {
        private Long refundId;
        private Long bookingId;
        private String bookingRef;
        private String tripTitle;
        private String travelerName;
        private String travelerEmail;
        private String travelerPhone;
        private java.math.BigDecimal originalAmount;
        private java.math.BigDecimal refundAmount;
        private String refundType;
        private String status;
        private String reason;
        private String adminNotes;
        private String requestedAt;
        private String processedAt;
        private String razorpayRefundId;
        private String processedByName;

        // Bank details
        private String accountHolderName;
        private String accountNumber;
        private String ifscCode;
        private String bankName;

        // Payment proof (admin fills after manual transfer)
        private String transactionReference;
        private String paymentMethod;
        private String paidOn;

        // Getters and Setters
        public Long getRefundId() { return refundId; }
        public void setRefundId(Long refundId) { this.refundId = refundId; }
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public String getBookingRef() { return bookingRef; }
        public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
        public String getTripTitle() { return tripTitle; }
        public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }
        public String getTravelerName() { return travelerName; }
        public void setTravelerName(String travelerName) { this.travelerName = travelerName; }
        public String getTravelerEmail() { return travelerEmail; }
        public void setTravelerEmail(String travelerEmail) { this.travelerEmail = travelerEmail; }
        public String getTravelerPhone() { return travelerPhone; }
        public void setTravelerPhone(String travelerPhone) { this.travelerPhone = travelerPhone; }
        public java.math.BigDecimal getOriginalAmount() { return originalAmount; }
        public void setOriginalAmount(java.math.BigDecimal originalAmount) { this.originalAmount = originalAmount; }
        public java.math.BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(java.math.BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        public String getRefundType() { return refundType; }
        public void setRefundType(String refundType) { this.refundType = refundType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
        public String getRequestedAt() { return requestedAt; }
        public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }
        public String getProcessedAt() { return processedAt; }
        public void setProcessedAt(String processedAt) { this.processedAt = processedAt; }
        public String getRazorpayRefundId() { return razorpayRefundId; }
        public void setRazorpayRefundId(String razorpayRefundId) { this.razorpayRefundId = razorpayRefundId; }
        public String getProcessedByName() { return processedByName; }
        public void setProcessedByName(String processedByName) { this.processedByName = processedByName; }
        public String getAccountHolderName() { return accountHolderName; }
        public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getIfscCode() { return ifscCode; }
        public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getPaidOn() { return paidOn; }
        public void setPaidOn(String paidOn) { this.paidOn = paidOn; }
    }

    // =========================================
    // REQUEST DTOs
    // =========================================
    public static class ApproveRefundRequest {
        private String transactionReference; // UTR / NEFT / IMPS reference number
        private String paymentMethod;        // BANK_TRANSFER, UPI, NEFT, IMPS, etc.
        private String adminNotes;           // Optional notes

        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }

    public static class RejectRefundRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
