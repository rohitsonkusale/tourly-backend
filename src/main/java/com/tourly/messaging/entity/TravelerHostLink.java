package com.tourly.messaging.entity;

import com.tourly.auth.entity.User;
import com.tourly.messaging.enums.LinkStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "traveler_host_links", uniqueConstraints = {
    @UniqueConstraint(name = "uq_traveler_host_links_pair", columnNames = {"traveler_id", "host_id"}),
    @UniqueConstraint(name = "uq_traveler_host_links_anonymous_id", columnNames = {"anonymous_id"})
})
public class TravelerHostLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id", nullable = false)
    private User traveler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "anonymous_id", nullable = false, unique = true, length = 20)
    private String anonymousId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LinkStatus status = LinkStatus.ANONYMOUS;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "revealed_at")
    private LocalDateTime revealedAt;

    public TravelerHostLink() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getTraveler() { return traveler; }
    public void setTraveler(User traveler) { this.traveler = traveler; }
    public User getHost() { return host; }
    public void setHost(User host) { this.host = host; }
    public String getAnonymousId() { return anonymousId; }
    public void setAnonymousId(String anonymousId) { this.anonymousId = anonymousId; }
    public LinkStatus getStatus() { return status; }
    public void setStatus(LinkStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getRevealedAt() { return revealedAt; }
    public void setRevealedAt(LocalDateTime revealedAt) { this.revealedAt = revealedAt; }
}
