package com.tourly.trip.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for trip edit history.
 * Groups changes by edit session for easy consumption.
 */
public class TripEditLogResponse {

    private Long tripId;
    private int totalEditCount;
    private List<EditSession> editSessions;

    public TripEditLogResponse() {}

    public TripEditLogResponse(Long tripId, int totalEditCount, List<EditSession> editSessions) {
        this.tripId = tripId;
        this.totalEditCount = totalEditCount;
        this.editSessions = editSessions;
    }

    // Getters & Setters
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public int getTotalEditCount() { return totalEditCount; }
    public void setTotalEditCount(int totalEditCount) { this.totalEditCount = totalEditCount; }

    public List<EditSession> getEditSessions() { return editSessions; }
    public void setEditSessions(List<EditSession> editSessions) { this.editSessions = editSessions; }

    /**
     * Represents one edit session (a single PUT request with multiple field changes).
     */
    public static class EditSession {
        private String editSessionId;
        private int editNumber;
        private String editedByName;
        private LocalDateTime editedAt;
        private String adminMessageContext; // Why admin asked for changes (before this edit)
        private List<FieldChange> changes;

        public EditSession() {}

        public String getEditSessionId() { return editSessionId; }
        public void setEditSessionId(String editSessionId) { this.editSessionId = editSessionId; }

        public int getEditNumber() { return editNumber; }
        public void setEditNumber(int editNumber) { this.editNumber = editNumber; }

        public String getEditedByName() { return editedByName; }
        public void setEditedByName(String editedByName) { this.editedByName = editedByName; }

        public LocalDateTime getEditedAt() { return editedAt; }
        public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }

        public String getAdminMessageContext() { return adminMessageContext; }
        public void setAdminMessageContext(String adminMessageContext) { this.adminMessageContext = adminMessageContext; }

        public List<FieldChange> getChanges() { return changes; }
        public void setChanges(List<FieldChange> changes) { this.changes = changes; }
    }

    /**
     * A single field change within an edit session.
     */
    public static class FieldChange {
        private String fieldName;
        private String oldValue;
        private String newValue;

        public FieldChange() {}

        public FieldChange(String fieldName, String oldValue, String newValue) {
            this.fieldName = fieldName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }

        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }

        public String getNewValue() { return newValue; }
        public void setNewValue(String newValue) { this.newValue = newValue; }
    }
}
