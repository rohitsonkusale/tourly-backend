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
     * Admin approves a pending refund and processes it via Razorpay.
     */
    void approveAndProcessRefund(Long refundId, Long adminUserId);

    /**
     * Admin rejects a pending refund.
     */
    void rejectRefund(Long refundId, Long adminUserId, String rejectionReason);
}