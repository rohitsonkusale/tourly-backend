package com.tourly.payment.service;

import com.tourly.payment.dto.request.AdminPayoutActionRequest;
import com.tourly.payment.dto.request.RequestPayoutRequest;
import com.tourly.payment.dto.response.AdminPayoutDetailResponse;
import com.tourly.payment.dto.response.BankAccountResponse;
import com.tourly.payment.dto.response.PayoutResponse;
import com.tourly.payment.dto.response.TripPayoutSummaryResponse;
import com.tourly.payment.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PayoutService {

    // Host operations
    PayoutResponse requestPayout(RequestPayoutRequest request);

    Page<PayoutResponse> getMyPayouts(Pageable pageable);

    List<BankAccountResponse> getMyBankAccounts();

    TripPayoutSummaryResponse getTripPayoutSummary(Long tripId);

    // Admin operations
    Page<PayoutResponse> getAllPayouts(Pageable pageable);

    Page<PayoutResponse> getPayoutsByStatus(PayoutStatus status, Pageable pageable);

    PayoutResponse processPayoutAction(Long payoutId, AdminPayoutActionRequest request);

    // Admin enriched detail
    AdminPayoutDetailResponse getPayoutDetail(Long payoutId);
}
