package com.tourly.trip.entity;

import com.tourly.auth.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Records each edit made to a trip.
 * Each row = one field that changed in a single edit session.
 * All rows sharing the same editSessionId belong to the same edit operation.
 */
@Entity
@Table(name = "trip_edit_logs", indexes = {
    @Index(name = "idx_trip_edit_logs_trip_id", columnList = "trip_id"),
    @Index(name = "idx_trip_edit_logs_session", columnList = "edit_session_id")
})
public class TripEditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by", nullable = false)
    private User editedBy;

    /** Groups all field changes from a single PUT request */
    @Column(name = "edit_session_id", nullable = false, length = 36)
    private String editSessionId;

    /** Which field was changed (e.g. "title", "basePrice", "stops") */
    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    /** Previous value (JSON string for complex types) */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /** New value (JSON string for complex types) */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** The admin message that was on the trip BEFORE this edit (context for why host edited) */
    @Column(name = "admin_message_context", length = 500)
    private String adminMessageContext;

    /** Edit number for this trip (1st edit, 2nd edit, etc.) */
    @Column(name = "edit_number", nullable = false)
    private Integer editNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TripEditLog() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public User getEditedBy() { return editedBy; }
    public void setEditedBy(User editedBy) { this.editedBy = editedBy; }

    public String getEditSessionId() { return editSessionId; }
    public void setEditSessionId(String editSessionId) { this.editSessionId = editSessionId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getAdminMessageContext() { return adminMessageContext; }
    public void setAdminMessageContext(String adminMessageContext) { this.adminMessageContext = adminMessageContext; }

    public Integer getEditNumber() { return editNumber; }
    public void setEditNumber(Integer editNumber) { this.editNumber = editNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
