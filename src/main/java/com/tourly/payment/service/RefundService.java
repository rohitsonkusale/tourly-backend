package com.tourly.payment.service;

import com.tourly.payment.dto.request.RefundRequest;
import com.tourly.payment.dto.response.RefundResponse;

public interface RefundService {

    RefundResponse processFullRefund(Long bookingId, RefundRequest request, String userEmail);

    /**
     * Creates refund request(s) in PENDING status (awaiting admin approval).
     * Called by auto-cancellation scheduler or manual cancellation flow.
     * Does NOT call Razorpay — only creates records for admin review.
     */
    void initiateAutoCancellationRefund(Long bookingId, String reason);

    /**
     * Creates refund request(s) in PENDING status with bank details for manual transfer.
     * Called by traveler-initiated cancellation flow.
     */
    void initiateAutoCancellationRefund(Long bookingId, String reason,
                                         String accountHolderName, String accountNumber,
                                         String ifscCode, String bankName);

    /**
     * Admin marks a pending refund as completed after manual bank transfer.
     * Records the UTR/transaction reference as proof of payment.
     */
    void approveAndProcessRefund(Long refundId, Long adminUserId,
                                  String transactionReference, String paymentMethod, String adminNotes);

    /**
     * Admin rejects a pending refund.
     */
    void rejectRefund(Long refundId, Long adminUserId, String rejectionReason);
}