package com.tourly.admin.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

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
    private LocalDate lastLoginDate;
    private LocalTime lastLoginTime;
    private LocalDate createdDate;
    private LocalTime createdTime;
    private LocalDate updatedDate;
    private LocalTime updatedTime;

    public AdminUserResponse() {
    }

    public AdminUserResponse(Long id, String fullName, String email, String phone, String roleName,
                             String accountStatus, Boolean emailVerified, Boolean phoneVerified,
                             Boolean kycVerified, LocalDate lastLoginDate, LocalTime lastLoginTime,
                             LocalDate createdDate, LocalTime createdTime,
                             LocalDate updatedDate, LocalTime updatedTime) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roleName = roleName;
        this.accountStatus = accountStatus;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.kycVerified = kycVerified;
        this.lastLoginDate = lastLoginDate;
        this.lastLoginTime = lastLoginTime;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updatedDate = updatedDate;
        this.updatedTime = updatedTime;
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
}