package com.tourly.payment.service;

import com.tourly.payment.dto.request.RefundRequest;
import com.tourly.payment.dto.response.RefundResponse;

public interface RefundService {

    RefundResponse processFullRefund(Long bookingId, RefundRequest request, String userEmail);
}