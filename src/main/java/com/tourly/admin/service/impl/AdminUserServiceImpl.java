package com.tourly.admin.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        return userRepository.findByDeletedAtIsNull(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public AdminUserResponse getUserById(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

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

        return userRepository.findByRole_NameAndDeletedAtIsNull(parsedRole, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<AdminUserResponse> getUsersByStatus(AccountStatus accountStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findByAccountStatusAndDeletedAtIsNull(accountStatus, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public AdminUserResponse suspendUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        user.setAccountStatus(AccountStatus.SUSPENDED);
        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    @Override
    public AdminUserResponse reactivateUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        user.setAccountStatus(AccountStatus.ACTIVE);
        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    // =========================
    // Mapper
    // =========================
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
                user.getLastLogin(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}