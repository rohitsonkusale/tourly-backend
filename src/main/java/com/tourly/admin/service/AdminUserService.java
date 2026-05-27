package com.tourly.admin.service;

import org.springframework.data.domain.Page;

import com.tourly.admin.dto.response.AdminUserResponse;
import com.tourly.auth.entity.AccountStatus;

public interface AdminUserService {

    Page<AdminUserResponse> getAllUsers(int page, int size);

    Page<AdminUserResponse> getDeletedUsers(int page, int size);

    AdminUserResponse getUserById(Long userId);

    Page<AdminUserResponse> getUsersByRole(String roleName, int page, int size);

    Page<AdminUserResponse> getUsersByStatus(AccountStatus accountStatus, int page, int size);

    AdminUserResponse suspendUser(Long userId);

    AdminUserResponse reactivateUser(Long userId);

    AdminUserResponse softDeleteUser(Long userId);

    AdminUserResponse restoreUser(Long userId);
}