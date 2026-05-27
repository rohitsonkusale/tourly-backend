package com.tourly.userprofile.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.tourly.auth.entity.User;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 150)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(length = 255)
    private String profilePictureUrl;

    @Column(length = 255)
    private String contactEmail;

    @Column(length = 50)
    private String contactPhone;

    @Column(length = 255)
    private String socialLinks; // JSON string or comma-separated links

    @Column(length = 100)
    private String preferredLanguage;

    @Column(length = 100)
    private String timezone;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // inside UserProfile entity
    @Column(length = 255)
    private String preferredDestinations; // comma-separated or JSON

    @Column(length = 255)
    private String travelStyles; // e.g., Adventure, Luxury, Solo

    @Column(length = 100)
    private Boolean newsletterSubscribed = true;
}