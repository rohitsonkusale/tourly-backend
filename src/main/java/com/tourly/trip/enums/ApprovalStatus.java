package com.tourly.trip.enums;

public enum ApprovalStatus {
    PENDING,        // Submitted, awaiting admin review
    APPROVED,       // Admin approved — trip is published
    REJECTED,       // Admin rejected — trip stays hidden
    PENDING_REVIEW; // Admin requests changes from host
}
