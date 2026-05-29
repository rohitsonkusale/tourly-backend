package com.tourly.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(nullable = true)
    private String password;

    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "aadhaar_number", unique = true, length = 12)
    private String aadhaarNumber;

    @Column(name = "pan_number", unique = true, length = 10)
    private String panNumber;

    @Column(name = "instagram_username", length = 255)
    private String instagramUsername;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    // Getter & Setter
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    @Column(name = "kyc_verified", nullable = false)
    private Boolean kycVerified = false;

    @Column(name = "admin_approval_flag", nullable = false)
    private Boolean adminApproved = false;

    @Column(name = "last_login")
    private LocalDate lastLoginDate;

    @Column(name = "last_login_time")
    private LocalTime lastLoginTime;

    @Column(name = "created_date", updatable = false)
    private LocalDate createdDate;

    @Column(name = "created_time", updatable = false)
    private LocalTime createdTime;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "updated_time")
    private LocalTime updatedTime;

    @Column(name = "deleted_date")
    private LocalDate deletedDate;

    @Column(name = "deleted_time")
    private LocalTime deletedTime;

    // =========================
    // Constructors
    // =========================
    public User() {
        this.accountStatus = AccountStatus.ACTIVE;
        this.emailVerified = false;
        this.phoneVerified = false;
        this.kycVerified = false;
        this.adminApproved = false;
    }

    public User(Long id, String fullName, String email, String phone, String password, AccountStatus accountStatus,
                Boolean emailVerified, Boolean phoneVerified, Boolean kycVerified, Boolean adminApproved, String aadhaarNumber, String panNumber,
                LocalDate lastLoginDate, LocalTime lastLoginTime,
                LocalDate createdDate, LocalTime createdTime,
                LocalDate updatedDate, LocalTime updatedTime,
                LocalDate deletedDate, LocalTime deletedTime) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.accountStatus = accountStatus;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.kycVerified = kycVerified;
        this.adminApproved = adminApproved;
        this.aadhaarNumber = aadhaarNumber;
        this.panNumber = panNumber;
        this.lastLoginDate = lastLoginDate;
        this.lastLoginTime = lastLoginTime;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updatedDate = updatedDate;
        this.updatedTime = updatedTime;
        this.deletedDate = deletedDate;
        this.deletedTime = deletedTime;
    }

    // =========================
    // Getters & Setters
    // =========================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getInstagramUsername() { return instagramUsername; }
    public void setInstagramUsername(String instagramUsername) { this.instagramUsername = instagramUsername; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public Boolean getKycVerified() { return kycVerified; }
    public void setKycVerified(Boolean kycVerified) { this.kycVerified = kycVerified; }

    public Boolean getAdminApproved() { return adminApproved; }
    public void setAdminApproved(Boolean adminApproved) { this.adminApproved = adminApproved; }

    public LocalDate getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDate lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    public LocalTime getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(LocalTime lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalTime createdTime) { this.createdTime = createdTime; }

    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }

    public LocalTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalTime updatedTime) { this.updatedTime = updatedTime; }

    public LocalDate getDeletedDate() { return deletedDate; }
    public void setDeletedDate(LocalDate deletedDate) { this.deletedDate = deletedDate; }

    public LocalTime getDeletedTime() { return deletedTime; }
    public void setDeletedTime(LocalTime deletedTime) { this.deletedTime = deletedTime; }

    // =========================
    // Lifecycle Hooks
    // =========================
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
        createdTime = LocalTime.now();
        updatedDate = LocalDate.now();
        updatedTime = LocalTime.now();
        if (accountStatus == null) accountStatus = AccountStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDate.now();
        updatedTime = LocalTime.now();
    }
}