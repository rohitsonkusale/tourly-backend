package com.tourly.booking.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CancelBookingRequest {

    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String reason;

    // Bank details for refund processing
    @Size(max = 100, message = "Account holder name cannot exceed 100 characters")
    private String accountHolderName;

    @Size(max = 20, message = "Account number cannot exceed 20 characters")
    @Pattern(regexp = "^$|^[0-9]{9,18}$", message = "Account number must be 9-18 digits")
    private String accountNumber;

    @Size(max = 11, message = "IFSC code cannot exceed 11 characters")
    @Pattern(regexp = "^$|^[A-Z]{4}0[A-Z0-9]{6}$", message = "IFSC code format is invalid (e.g., SBIN0001234)")
    private String ifscCode;

    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName != null ? accountHolderName.trim() : null;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber != null ? accountNumber.trim() : null;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode != null ? ifscCode.trim().toUpperCase() : null;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName != null ? bankName.trim() : null;
    }
}