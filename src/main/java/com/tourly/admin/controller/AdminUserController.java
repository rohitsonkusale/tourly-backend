package com.tourly.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.admin.dto.response.AdminUserResponse;
import com.tourly.admin.service.AdminUserService;
import com.tourly.auth.entity.AccountStatus;
import com.tourly.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/api/admin/users")
@Validated
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin User Management", description = "Admin APIs for managing users, roles, statuses, and soft deletes")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "Get all active users", description = "Fetch paginated list of all active (non-deleted) users")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Page must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Size must be greater than 0") int size) {

        Page<AdminUserResponse> response = adminUserService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Active users fetched successfully", response));
    }

    @GetMapping("/deleted")
    @Operation(summary = "Get deleted users", description = "Fetch paginated list of all soft-deleted users")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getDeletedUsers(
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Page must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Size must be greater than 0") int size) {

        Page<AdminUserResponse> response = adminUserService.getDeletedUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Deleted users fetched successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Fetch a specific user by ID")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long id) {

        AdminUserResponse response = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }

    @GetMapping("/role/{roleName}")
    @Operation(summary = "Get users by role", description = "Fetch paginated users filtered by role name")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getUsersByRole(
            @PathVariable String roleName,
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Page must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Size must be greater than 0") int size) {

        Page<AdminUserResponse> response = adminUserService.getUsersByRole(roleName, page, size);
        return ResponseEntity.ok(ApiResponse.success("Users by role fetched successfully", response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get users by account status", description = "Fetch paginated users filtered by account status")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getUsersByStatus(
            @PathVariable AccountStatus status,
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Page must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Size must be greater than 0") int size) {

        Page<AdminUserResponse> response = adminUserService.getUsersByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Users by status fetched successfully", response));
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspend user", description = "Suspend a user account by ID")
    public ResponseEntity<ApiResponse<AdminUserResponse>> suspendUser(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long id) {

        AdminUserResponse response = adminUserService.suspendUser(id);
        return ResponseEntity.ok(ApiResponse.success("User suspended successfully", response));
    }

    @PutMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate user", description = "Reactivate a suspended user account by ID")
    public ResponseEntity<ApiResponse<AdminUserResponse>> reactivateUser(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long id) {

        AdminUserResponse response = adminUserService.reactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User reactivated successfully", response));
    }

    @PutMapping("/{id}/soft-delete")
    @Operation(summary = "Soft delete user", description = "Soft delete a user account by ID")
    public ResponseEntity<ApiResponse<AdminUserResponse>> softDeleteUser(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long id) {

        AdminUserResponse response = adminUserService.softDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User soft-deleted successfully", response));
    }

    @PutMapping("/{id}/restore")
    @Operation(summary = "Restore user", description = "Restore a soft-deleted user account by ID")
    public ResponseEntity<ApiResponse<AdminUserResponse>> restoreUser(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long id) {

        AdminUserResponse response = adminUserService.restoreUser(id);
        return ResponseEntity.ok(ApiResponse.success("User restored successfully", response));
    }
}