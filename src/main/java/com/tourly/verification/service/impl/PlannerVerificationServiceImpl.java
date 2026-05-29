package com.tourly.verification.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import com.tourly.auth.entity.Role;
import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.verification.dto.request.PlannerVerificationRequest;
import com.tourly.verification.dto.response.PlannerVerificationResponse;
import com.tourly.verification.entity.PlannerVerification;
import com.tourly.verification.enums.VerificationStatus;
import com.tourly.verification.repository.PlannerVerificationRepository;
import com.tourly.verification.service.PlannerVerificationService;

@Service
@Transactional
public class PlannerVerificationServiceImpl implements PlannerVerificationService {

    private final PlannerVerificationRepository plannerVerificationRepository;
    private final UserRepository userRepository;

    public PlannerVerificationServiceImpl(PlannerVerificationRepository plannerVerificationRepository,
                                          UserRepository userRepository) {
        this.plannerVerificationRepository = plannerVerificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PlannerVerificationResponse applyVerification(PlannerVerificationRequest request) {
        User currentUser = getCurrentUser();

        validateEligibleRole(currentUser);

        PlannerVerification verification = plannerVerificationRepository
                .findByUserId(currentUser.getId())
                .orElse(null);

        if (verification == null) {
            verification = new PlannerVerification();
            verification.setUser(currentUser);
        } else {
            validateReapplyAllowed(verification);
        }

        verification.setDisplayName(request.getDisplayName().trim());
        verification.setBio(request.getBio() != null ? request.getBio().trim() : null);
        verification.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
        verification.setExperienceYears(request.getExperienceYears());
        currentUser.setAadhaarNumber(request.getAadhaarNumber().trim());
        currentUser.setPanNumber(request.getPanNumber().trim().toUpperCase());
        userRepository.save(currentUser);
        verification.setAadhaarDocumentUrl(request.getAadhaarDocumentUrl() != null ? request.getAadhaarDocumentUrl().trim() : null);
        verification.setPanDocumentUrl(request.getPanDocumentUrl() != null ? request.getPanDocumentUrl().trim() : null);

        verification.setVerificationStatus(VerificationStatus.PENDING);
        verification.setRejectionReason(null);
        verification.setSubmittedAt(LocalDateTime.now());
        verification.setReviewedAt(null);
        verification.setReviewedBy(null);

        PlannerVerification saved = plannerVerificationRepository.save(verification);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PlannerVerificationResponse getMyVerification() {
        User currentUser = getCurrentUser();

        PlannerVerification verification = plannerVerificationRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Verification record not found for current user"));

        return mapToResponse(verification);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void validateEligibleRole(User user) {
        RoleName role = user.getRole().getName();

        if (role != RoleName.PLANNER && role != RoleName.HOST && role != RoleName.ADMIN) {
            throw new BadRequestException("Only planner, host, or admin users can apply for verification");
        }
    }

    private void validateReapplyAllowed(PlannerVerification verification) {
        VerificationStatus status = verification.getVerificationStatus();

        if (status == VerificationStatus.PENDING) {
            throw new BadRequestException("Verification request is already pending review");
        }

        if (status == VerificationStatus.APPROVED) {
            throw new BadRequestException("Verification is already approved");
        }

        if (status == VerificationStatus.SUSPENDED) {
            throw new BadRequestException("Verification is suspended. Please contact admin");
        }

        // REJECTED is allowed for re-apply
    }

    private PlannerVerificationResponse mapToResponse(PlannerVerification verification) {
        PlannerVerificationResponse response = new PlannerVerificationResponse();

        response.setId(verification.getId());
        response.setUserId(verification.getUser().getId());
        response.setDisplayName(verification.getDisplayName());
        response.setBio(verification.getBio());
        response.setSpecialization(verification.getSpecialization());
        response.setExperienceYears(verification.getExperienceYears());

        response.setMaskedAadhaarNumber(maskAadhaar(verification.getUser().getAadhaarNumber()));
        response.setMaskedPanNumber(maskPan(verification.getUser().getPanNumber()));

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