package com.tourly.auth.dto.request;

public class RegisterRequest {

    private String fullName;
    private String email;
    private String phone;
    private String password;

    private String roleName; // should match RoleName enum: "TRAVELER", "PLANNER", "HOST"

    // Getters & Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Getter & Setter
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}