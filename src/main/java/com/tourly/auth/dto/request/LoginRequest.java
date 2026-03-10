package com.tourly.auth.dto.request;

public class LoginRequest {

    private String emailOrPhone;
    private String password;

    // Getters & Setters
    public String getEmailOrPhone() { return emailOrPhone; }
    public void setEmailOrPhone(String emailOrPhone) { this.emailOrPhone = emailOrPhone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}