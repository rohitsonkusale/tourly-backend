package com.tourly.verification.dto.response;

import java.time.LocalDateTime;
import com.tourly.verification.enums.VerificationStatus;

public class HostVerificationResponse {

    private Long id;
    private Long userId;
    private String displayName;
    private String bio;
    private String specialization;
    private Integer experienceYears;

    private String maskedAadhaarNumber;
    private String maskedPanNumber;

    private String aadhaarDocumentUrl;
    private String panDocumentUrl;
    private String selfieUrl;

    private VerificationStatus verificationStatus;
    private String rejectionReason;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedByUserId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public HostVerificationResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getMaskedAadhaarNumber() {
        return maskedAadhaarNumber;
    }

    public void setMaskedAadhaarNumber(String maskedAadhaarNumber) {
        this.maskedAadhaarNumber = maskedAadhaarNumber;
    }

    public String getMaskedPanNumber() {
        return maskedPanNumber;
    }

    public void setMaskedPanNumber(String maskedPanNumber) {
        this.maskedPanNumber = maskedPanNumber;
    }

    public String getAadhaarDocumentUrl() {
        return aadhaarDocumentUrl;
    }

    public void setAadhaarDocumentUrl(String aadhaarDocumentUrl) {
        this.aadhaarDocumentUrl = aadhaarDocumentUrl;
    }

    public String getPanDocumentUrl() {
        return panDocumentUrl;
    }

    public void setPanDocumentUrl(String panDocumentUrl) {
        this.panDocumentUrl = panDocumentUrl;
    }

    public String getSelfieUrl() {
        return selfieUrl;
    }

    public void setSelfieUrl(String selfieUrl) {
        this.selfieUrl = selfieUrl;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Long getReviewedByUserId() {
        return reviewedByUserId;
    }

    public void setReviewedByUserId(Long reviewedByUserId) {
        this.reviewedByUserId = reviewedByUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

