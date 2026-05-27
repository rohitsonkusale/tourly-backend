package com.tourly.verification.service;

import com.tourly.verification.dto.request.PlannerVerificationRequest;
import com.tourly.verification.dto.response.PlannerVerificationResponse;

public interface PlannerVerificationService {

    PlannerVerificationResponse applyVerification(PlannerVerificationRequest request);

    PlannerVerificationResponse getMyVerification();
}