package com.tourly.verification.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.AccountStatus;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.entity.HostVerification;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.trip.enums.ApprovalStatus;
import com.tourly.verification.dto.request.AdminVerificationActionRequest;
import com.tourly.verification.dto.response.HostVerificationResponse;
import com.tourly.verification.repository.HostVerificationRepository;
import com.tourly.verification.service.AdminHostVerificationService;

@Service
@Transactional
public class AdminHostVerificationServiceImpl implements AdminHostVerificationService {

    private final HostVerificationRepository hostVerificationRepository;
    private final UserRepository userRepository;

    public AdminHostVerificationServiceImpl(HostVerificationRepository hostVerificationRepository,
                                            UserRepository userRepository) {
        this.hostVerificationRepository = hostVerificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HostVerificationResponse> getPendingVerifications() {
        return hostVerificationRepository.findByStatusWithUser(ApprovalStatus.PENDING)
                .stream()
                .filter(v -> v.getAadhaarDocumentUrl() != null
                        || v.getPanDocumentUrl() != null
                        || v.getSelfieUrl() != null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HostVerificationResponse> getVerificationsByStatus(ApprovalStatus status) {
        return hostVerificationRepository.findByStatusWithUser(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HostVerificationResponse getVerificationById(Long verificationId) {
        HostVerification verification = getVerificationOrThrow(verificationId);
        return mapToResponse(verification);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public HostVerificationResponse approveVerification(Long verificationId) {
        HostVerification verification = getVerificationOrThrow(verificationId);

        // Allow approve on PENDING or PENDING_REVIEW
        if (verification.getVerificationStatus() != ApprovalStatus.PENDING
                && verification.getVerificationStatus() != ApprovalStatus.PENDING_REVIEW) {
            throw new BadRequestException("Can only approve PENDING or PENDING_REVIEW verifications");
        }

        User adminUser = getCurrentAdmin();
        User applicant = verification.getUser();

        verification.setVerificationStatus(ApprovalStatus.APPROVED);
        verification.setRejectionReason(null);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(adminUser);

        // Update applicant details
        applicant.setKycVerified(true);
        applicant.setAdminApproved(true);
        applicant.setAccountStatus(AccountStatus.ACTIVE);

        userRepository.save(applicant);
        HostVerification saved = hostVerificationRepository.save(verification);

        return mapToResponse(saved);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public HostVerificationResponse rejectVerification(Long verificationId, AdminVerificationActionRequest request) {
        HostVerification verification = getVerificationOrThrow(verificationId);

        // Allow reject on PENDING or PENDING_REVIEW
        if (verification.getVerificationStatus() != ApprovalStatus.PENDING
                && verification.getVerificationStatus() != ApprovalStatus.PENDING_REVIEW) {
            throw new BadRequestException("Can only reject PENDING or PENDING_REVIEW verifications");
        }

        if (request == null || request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("Rejection reason is required");
        }

        User adminUser = getCurrentAdmin();
        User applicant = verification.getUser();

        verification.setVerificationStatus(ApprovalStatus.REJECTED);
        verification.setRejectionReason(request.getReason().trim());
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(adminUser);

        applicant.setKycVerified(false);
        applicant.setAdminApproved(false);
        applicant.setAccountStatus(AccountStatus.PENDING_VERIFICATION);

        userRepository.save(applicant);
        HostVerification saved = hostVerificationRepository.save(verification);

        return mapToResponse(saved);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public HostVerificationResponse requestChanges(Long verificationId, AdminVerificationActionRequest request) {
        HostVerification verification = getVerificationOrThrow(verificationId);

        // Allow on PENDING or PENDING_REVIEW (admin can update their message)
        if (verification.getVerificationStatus() != ApprovalStatus.PENDING
                && verification.getVerificationStatus() != ApprovalStatus.PENDING_REVIEW) {
            throw new BadRequestException("Can only request changes on PENDING or PENDING_REVIEW verifications");
        }

        if (request == null || request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("A message explaining required changes is mandatory");
        }

        User adminUser = getCurrentAdmin();
        User applicant = verification.getUser();

        verification.setVerificationStatus(ApprovalStatus.PENDING_REVIEW);
        verification.setRejectionReason(request.getReason().trim());
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(adminUser);

        applicant.setKycVerified(false);

        userRepository.save(applicant);
        HostVerification saved = hostVerificationRepository.save(verification);
        return mapToResponse(saved);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public HostVerificationResponse suspendVerification(Long verificationId, AdminVerificationActionRequest request) {
        HostVerification verification = getVerificationOrThrow(verificationId);

        if (verification.getVerificationStatus() != ApprovalStatus.APPROVED) {
            throw new BadRequestException("Only approved verification requests can be suspended");
        }

        if (request == null || request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("Suspension reason is required");
        }

        User adminUser = getCurrentAdmin();
        User applicant = verification.getUser();

        // ApprovalStatus doesn't have SUSPENDED, so we use REJECTED to signify revoked status
        verification.setVerificationStatus(ApprovalStatus.REJECTED);
        verification.setRejectionReason(request.getReason().trim());
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(adminUser);

        applicant.setKycVerified(false);
        applicant.setAdminApproved(false);
        applicant.setAccountStatus(AccountStatus.SUSPENDED);

        userRepository.save(applicant);
        HostVerification saved = hostVerificationRepository.save(verification);

        return mapToResponse(saved);
    }

    private HostVerification getVerificationOrThrow(Long verificationId) {
        return hostVerificationRepository.findByIdWithUser(verificationId)
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

    private HostVerificationResponse mapToResponse(HostVerification verification) {
        HostVerificationResponse response = new HostVerificationResponse();

        response.setId(verification.getId());

        User user = verification.getUser();
        if (user != null) {
            response.setUserId(user.getId());
            response.setMaskedAadhaarNumber(user.getAadhaarNumber());
            response.setMaskedPanNumber(user.getPanNumber());
        }

        response.setDisplayName(verification.getDisplayName());
        response.setBio(verification.getBio());
        response.setSpecialization(verification.getSpecialization());
        response.setExperienceYears(verification.getExperienceYears());

        // Return actual document URLs/base64 data for admin review
        response.setAadhaarDocumentUrl(verification.getAadhaarDocumentUrl());
        response.setPanDocumentUrl(verification.getPanDocumentUrl());
        response.setSelfieUrl(verification.getSelfieUrl());

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
