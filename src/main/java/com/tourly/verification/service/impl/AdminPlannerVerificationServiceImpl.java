package com.tourly.verification.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.verification.dto.request.AdminVerificationActionRequest;
import com.tourly.verification.dto.response.PlannerVerificationResponse;
import com.tourly.verification.entity.PlannerVerification;
import com.tourly.verification.enums.VerificationStatus;
import com.tourly.verification.repository.PlannerVerificationRepository;
import com.tourly.verification.service.AdminPlannerVerificationService;

@Service
@Transactional
public class AdminPlannerVerificationServiceImpl implements AdminPlannerVerificationService {

    private final PlannerVerificationRepository plannerVerificationRepository;
    private final UserRepository userRepository;

    public AdminPlannerVerificationServiceImpl(PlannerVerificationRepository plannerVerificationRepository,
                                               UserRepository userRepository) {
        this.plannerVerificationRepository = plannerVerificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlannerVerificationResponse> getPendingVerifications() {
        return plannerVerificationRepository.findByVerificationStatusOrderBySubmittedAtAsc(VerificationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlannerVerificationResponse> getVerificationsByStatus(VerificationStatus status) {
        return plannerVerificationRepository.findByVerificationStatusOrderBySubmittedAtAsc(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlannerVerificationResponse getVerificationById(Long verificationId) {
        PlannerVerification verification = getVerificationOrThrow(verificationId);
        return mapToResponse(verification);
    }

    @Override
    public PlannerVerificationResponse approveVerification(Long verificationId) {
        PlannerVerification verification = getVerificationOrThrow(verificationId);

        // STRICT RULE: only PENDING can be approved
        if (verification.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new BadRequestException("Only pending verification requests can be approved");
        }

        User adminUser = getCurrentAdmin();
        User applicant = verification.getUser();

        verification.setVerificationStatus(VerificationStatus.APPROVED);
        verification.setRejectionReason(null);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(adminUser);

        // IMPORTANT: verification approval should enable KYC access
        applicant.setKycVerified(true);

        // OPTIONAL: uncomment only if your business requires account activation on approval
        // applicant.setAccountStatus(AccountStatus.ACTIVE);

        userRepository.save(applicant);
        PlannerVerification saved = plannerVerificationRepository.save(verification);

        return mapToResponse(saved);
    }

    @Override
    public PlannerVerificationResponse rejectVerification(Long verificationId, AdminVerificationActionRequest request) {
        PlannerVerification verification = getVerificationOrThrow(verificationId);

        // STRICT RULE: only PENDING can be rejected
        if (verification.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new BadRequestException("Only pending verification requests can be rejected");
        }

        if (request == null || request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("Rejection reason is required");
        }

        User adminUser = getCurrentAdmin();
        User applicant = verification.getUser();

        verification.setVerificationStatus(VerificationStatus.REJECTED);
        verification.setRejectionReason(request.getReason().trim());
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(adminUser);

        // IMPORTANT: rejected user should not remain KYC verified
        applicant.setKycVerified(false);

        userRepository.save(applicant);
        PlannerVerification saved = plannerVerificationRepository.save(verification);

        return mapToResponse(saved);
    }

    @Override
    public PlannerVerificationResponse suspendVerification(Long verificationId, AdminVerificationActionRequest request) {
        PlannerVerification verification = getVerificationOrThrow(verificationId);

        // STRICT RULE: only APPROVED can be suspended
        if (verification.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new BadRequestException("Only approved verification requests can be suspended");
        }

        if (request == null || request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("Suspension reason is required");
        }

        User adminUser = getCurrentAdmin();
        User applicant = verification.getUser();

        verification.setVerificationStatus(VerificationStatus.SUSPENDED);
        verification.setRejectionReason(request.getReason().trim());
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(adminUser);

        // IMPORTANT: suspended user should lose KYC access
        applicant.setKycVerified(false);

        userRepository.save(applicant);
        PlannerVerification saved = plannerVerificationRepository.save(verification);

        return mapToResponse(saved);
    }

    private PlannerVerification getVerificationOrThrow(Long verificationId) {
        return plannerVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request not found with id: " + verificationId));
    }

    private User getCurrentAdmin() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Admin user is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated admin user not found"));
    }

    private PlannerVerificationResponse mapToResponse(PlannerVerification verification) {
        PlannerVerificationResponse response = new PlannerVerificationResponse();

        response.setId(verification.getId());
        response.setUserId(verification.getUser().getId());
        response.setDisplayName(verification.getDisplayName());
        response.setBio(verification.getBio());
        response.setSpecialization(verification.getSpecialization());
        response.setExperienceYears(verification.getExperienceYears());

        response.setMaskedAadhaarNumber(maskAadhaar(verification.getAadhaarNumber()));
        response.setMaskedPanNumber(maskPan(verification.getPanNumber()));

        response.setAadhaarDocumentUrl(verification.getAadhaarDocumentUrl());
        response.setPanDocumentUrl(verification.getPanDocumentUrl());

        response.setVerificationStatus(verification.getVerificationStatus());
        response.setRejectionReason(verification.getRejectionReason());

        response.setSubmittedAt(verification.getSubmittedAt());
        response.setReviewedAt(verification.getReviewedAt());
        response.setReviewedByUserId(
                verification.getReviewedBy() != null ? verification.getReviewedBy().getId() : null
        );

        response.setCreatedAt(verification.getCreatedAt());
        response.setUpdatedAt(verification.getUpdatedAt());

        return response;
    }

    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() != 12) {
            return null;
        }
        return "XXXXXXXX" + aadhaar.substring(8);
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() != 10) {
            return null;
        }
        return pan.substring(0, 3) + "XXXXX" + pan.substring(8);
    }
}