package com.tourly.admin.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tourly.admin.dto.response.AdminUserResponse;
import com.tourly.admin.service.AdminUserService;
import com.tourly.auth.entity.AccountStatus;
import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    public AdminUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<AdminUserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findByDeletedDateIsNull(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<AdminUserResponse> getDeletedUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findByDeletedDateIsNotNull(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public AdminUserResponse getUserById(Long userId) {
        User user = userRepository.findByIdAndDeletedDateIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("Active user not found with ID: " + userId));

        return mapToResponse(user);
    }

    @Override
    public Page<AdminUserResponse> getUsersByRole(String roleName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        RoleName parsedRole;
        try {
            parsedRole = RoleName.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role name: " + roleName);
        }

        return userRepository.findByRole_NameAndDeletedDateIsNull(parsedRole, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<AdminUserResponse> getUsersByStatus(AccountStatus accountStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findByAccountStatusAndDeletedDateIsNull(accountStatus, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public AdminUserResponse suspendUser(Long userId) {
        User user = userRepository.findByIdAndDeletedDateIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("Active user not found with ID: " + userId));

        String currentAdminEmail = getCurrentLoggedInUserEmail();
        if (currentAdminEmail != null && currentAdminEmail.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalStateException("You cannot suspend your own account.");
        }

        if (user.getRole() != null && user.getRole().getName() == RoleName.ADMIN) {
            throw new IllegalStateException("Admin accounts cannot be suspended.");
        }

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new IllegalStateException("User is already suspended.");
        }

        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new IllegalStateException("Blocked users cannot be suspended.");
        }

        if (user.getAccountStatus() == AccountStatus.DELETED) {
            throw new IllegalStateException("Deleted users cannot be suspended.");
        }

        user.setAccountStatus(AccountStatus.SUSPENDED);
        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    @Override
    public AdminUserResponse reactivateUser(Long userId) {
        User user = userRepository.findByIdAndDeletedDateIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("Active user not found with ID: " + userId));

        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new IllegalStateException("User is already active.");
        }

        if (user.getAccountStatus() == AccountStatus.DELETED) {
            throw new IllegalStateException("Deleted users cannot be reactivated.");
        }

        user.setAccountStatus(AccountStatus.ACTIVE);
        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    @Override
    public AdminUserResponse softDeleteUser(Long userId) {
        User user = userRepository.findByIdAndDeletedDateIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("Active user not found with ID: " + userId));

        String currentAdminEmail = getCurrentLoggedInUserEmail();
        if (currentAdminEmail != null && currentAdminEmail.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalStateException("You cannot delete your own account.");
        }

        if (user.getRole() != null && user.getRole().getName() == RoleName.ADMIN) {
            throw new IllegalStateException("Admin accounts cannot be deleted.");
        }

        if (user.getAccountStatus() == AccountStatus.DELETED) {
            throw new IllegalStateException("User is already marked as deleted.");
        }

        user.setDeletedDate(LocalDate.now());
        user.setDeletedTime(LocalTime.now());
        user.setAccountStatus(AccountStatus.DELETED);

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public AdminUserResponse restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        if (user.getDeletedDate() == null) {
            throw new IllegalStateException("User is not soft deleted.");
        }

        user.setDeletedDate(null);
        user.setDeletedTime(null);
        user.setAccountStatus(AccountStatus.ACTIVE);

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    // =========================
    // Helper Methods
    // =========================
    private String getCurrentLoggedInUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getName(); // Usually email in JWT-based auth
    }

    private AdminUserResponse mapToResponse(User user) {
        String roleName = null;
        if (user.getRole() != null && user.getRole().getName() != null) {
            roleName = user.getRole().getName().name();
        }

        return new AdminUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                roleName,
                user.getAccountStatus() != null ? user.getAccountStatus().name() : null,
                user.getEmailVerified(),
                user.getPhoneVerified(),
                user.getKycVerified(),
                user.getLastLoginDate(),
                user.getLastLoginTime(),
                user.getCreatedDate(),
                user.getCreatedTime(),
                user.getUpdatedDate(),
                user.getUpdatedTime()
        );
    }
}