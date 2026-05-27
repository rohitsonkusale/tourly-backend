package com.tourly.verification.service;

import java.util.List;

import com.tourly.verification.dto.request.AdminVerificationActionRequest;
import com.tourly.verification.dto.response.PlannerVerificationResponse;
import com.tourly.verification.enums.VerificationStatus;

public interface AdminPlannerVerificationService {

    List<PlannerVerificationResponse> getPendingVerifications();

    List<PlannerVerificationResponse> getVerificationsByStatus(VerificationStatus status);

    PlannerVerificationResponse getVerificationById(Long verificationId);

    PlannerVerificationResponse approveVerification(Long verificationId);

    PlannerVerificationResponse rejectVerification(Long verificationId, AdminVerificationActionRequest request);

    PlannerVerificationResponse suspendVerification(Long verificationId, AdminVerificationActionRequest request);
}