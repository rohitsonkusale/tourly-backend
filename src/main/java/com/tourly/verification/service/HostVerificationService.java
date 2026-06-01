package com.tourly.verification.service;

import com.tourly.verification.dto.request.HostVerificationRequest;
import com.tourly.verification.dto.response.HostVerificationResponse;

public interface HostVerificationService {

    /** Host submits or resubmits their KYC verification */
    HostVerificationResponse applyVerification(HostVerificationRequest request);

    /** Host checks their own KYC status */
    HostVerificationResponse getMyVerification();
}
