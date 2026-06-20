package com.tourly.messaging.entity;

import com.tourly.auth.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks contact-sharing violation attempts by users.
 * Used for the 3-strike system and audit logging.
 */
@Entity
@Table(name = "contact_violations")
public class ContactViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "violation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "original_content", nullable = false, length = 1000)
    private String originalContent;

    @Column(name = "masked_content", nullable = false, length = 1000)
    private String maskedContent;

    @Column(name = "detected_type", nullable = false, length = 30)
    private String detectedType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ContactViolation() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
    public String getMaskedContent() { return maskedContent; }
    public void setMaskedContent(String maskedContent) { this.maskedContent = maskedContent; }
    public String getDetectedType() { return detectedType; }
    public void setDetectedType(String detectedType) { this.detectedType = detectedType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
