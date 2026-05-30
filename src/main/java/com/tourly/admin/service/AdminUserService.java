package com.tourly.admin.service;

import java.util.List;

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

    // Pending approval queue — HOST and PLANNER users with adminApproved = N
    List<AdminUserResponse> getPendingHostApprovals();

    List<AdminUserResponse> getPendingPlannerApprovals();

    // Approve a user — sets adminApproved = Y and accountStatus = ACTIVE
    AdminUserResponse approveUser(Long userId);
}