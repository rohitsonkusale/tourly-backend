package com.tourly.verification.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.entity.HostVerification;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.trip.enums.ApprovalStatus;
import com.tourly.verification.dto.request.HostVerificationRequest;
import com.tourly.verification.dto.response.HostVerificationResponse;
import com.tourly.verification.repository.HostVerificationRepository;
import com.tourly.verification.service.HostVerificationService;

@Service
@Transactional
public class HostVerificationServiceImpl implements HostVerificationService {

    private final HostVerificationRepository hostVerificationRepository;
    private final UserRepository userRepository;

    public HostVerificationServiceImpl(HostVerificationRepository hostVerificationRepository,
                                       UserRepository userRepository) {
        this.hostVerificationRepository = hostVerificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public HostVerificationResponse applyVerification(HostVerificationRequest request) {
        User currentUser = getCurrentUser();
        validateEligibleRole(currentUser);

        HostVerification verification = hostVerificationRepository
                .findByUserId(currentUser.getId())
                .orElse(null);

        if (verification == null) {
            verification = new HostVerification();
            verification.setUser(currentUser);
        } else {
            validateReapplyAllowed(verification);
        }

        verification.setDisplayName(request.getDisplayName().trim());
        verification.setBio(request.getBio() != null ? request.getBio().trim() : null);
        verification.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
        verification.setExperienceYears(request.getExperienceYears());

        // Save Aadhaar and PAN numbers to user table
        currentUser.setAadhaarNumber(request.getAadhaarNumber().trim());
        currentUser.setPanNumber(request.getPanNumber().trim().toUpperCase());
        userRepository.save(currentUser);

        verification.setAadhaarDocumentUrl(request.getAadhaarDocumentUrl() != null ? request.getAadhaarDocumentUrl().trim() : null);
        verification.setPanDocumentUrl(request.getPanDocumentUrl() != null ? request.getPanDocumentUrl().trim() : null);
        verification.setSelfieUrl(request.getSelfieUrl() != null ? request.getSelfieUrl().trim() : null);

        verification.setVerificationStatus(ApprovalStatus.PENDING);
        verification.setRejectionReason(null);
        verification.setSubmittedAt(LocalDateTime.now());
        verification.setReviewedAt(null);
        verification.setReviewedBy(null);

        HostVerification saved = hostVerificationRepository.save(verification);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HostVerificationResponse getMyVerification() {
        User currentUser = getCurrentUser();

        HostVerification verification = hostVerificationRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No verification record found. Please submit your KYC first."));

        return mapToResponse(verification);
    }

    // ── Helpers ──────────────────────────────────────────────

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new BadRequestException("User is not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void validateEligibleRole(User user) {
        RoleName role = user.getRole().getName();
        if (role != RoleName.HOST && role != RoleName.ADMIN) {
            throw new BadRequestException("Only HOST users can apply for host verification");
        }
    }

    private void validateReapplyAllowed(HostVerification verification) {
        ApprovalStatus status = verification.getVerificationStatus();

        // Allow resubmission if documents were never uploaded (incomplete submission)
        boolean hasDocuments = verification.getAadhaarDocumentUrl() != null
                || verification.getPanDocumentUrl() != null
                || verification.getSelfieUrl() != null;

        if (status == ApprovalStatus.PENDING && !hasDocuments) {
            // Incomplete record — allow overwrite
            return;
        }
        if (status == ApprovalStatus.PENDING) {
            throw new BadRequestException("Your verification is already pending review. Please wait for admin decision.");
        }
        if (status == ApprovalStatus.APPROVED) {
            throw new BadRequestException("Your verification is already approved.");
        }
        // REJECTED and PENDING_REVIEW are allowed to resubmit
    }

    private HostVerificationResponse mapToResponse(HostVerification v) {
        HostVerificationResponse r = new HostVerificationResponse();
        r.setId(v.getId());
        r.setUserId(v.getUser().getId());
        r.setDisplayName(v.getDisplayName());
        r.setBio(v.getBio());
        r.setSpecialization(v.getSpecialization());
        r.setExperienceYears(v.getExperienceYears());
        r.setMaskedAadhaarNumber(maskAadhaar(v.getUser().getAadhaarNumber()));
        r.setMaskedPanNumber(maskPan(v.getUser().getPanNumber()));
        r.setAadhaarDocumentUrl(v.getAadhaarDocumentUrl());
        r.setPanDocumentUrl(v.getPanDocumentUrl());
        r.setSelfieUrl(v.getSelfieUrl());
        r.setVerificationStatus(v.getVerificationStatus());
        r.setRejectionReason(v.getRejectionReason());
        r.setSubmittedAt(v.getSubmittedAt());
        r.setReviewedAt(v.getReviewedAt());
        r.setReviewedByUserId(v.getReviewedBy() != null ? v.getReviewedBy().getId() : null);
        r.setCreatedAt(v.getCreatedAt());
        r.setUpdatedAt(v.getUpdatedAt());
        return r;
    }

    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() != 12) return null;
        return "XXXXXXXX" + aadhaar.substring(8);
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() != 10) return null;
        return pan.substring(0, 3) + "XXXXX" + pan.substring(8);
    }
}
