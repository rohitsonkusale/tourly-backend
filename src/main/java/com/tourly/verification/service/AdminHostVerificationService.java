package com.tourly.verification.service;

import java.util.List;
import com.tourly.verification.dto.request.AdminVerificationActionRequest;
import com.tourly.verification.dto.response.HostVerificationResponse;
import com.tourly.trip.enums.ApprovalStatus;

public interface AdminHostVerificationService {

    List<HostVerificationResponse> getPendingVerifications();

    List<HostVerificationResponse> getVerificationsByStatus(ApprovalStatus status);

    HostVerificationResponse getVerificationById(Long verificationId);

    HostVerificationResponse approveVerification(Long verificationId);

    HostVerificationResponse rejectVerification(Long verificationId, AdminVerificationActionRequest request);

    HostVerificationResponse suspendVerification(Long verificationId, AdminVerificationActionRequest request);
}
