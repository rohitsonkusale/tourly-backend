package com.tourly.admin.dto.response;

import java.time.LocalDateTime;

public class AdminUserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String roleName;
    private String accountStatus;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean kycVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminUserResponse() {
    }

    public AdminUserResponse(Long id, String fullName, String email, String phone, String roleName,
                             String accountStatus, Boolean emailVerified, Boolean phoneVerified,
                             Boolean kycVerified, LocalDateTime lastLogin,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roleName = roleName;
        this.accountStatus = accountStatus;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.kycVerified = kycVerified;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public Boolean getKycVerified() { return kycVerified; }
    public void setKycVerified(Boolean kycVerified) { this.kycVerified = kycVerified; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}