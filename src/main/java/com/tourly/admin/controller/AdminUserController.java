package com.tourly.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tourly.admin.dto.response.AdminUserResponse;
import com.tourly.admin.service.AdminUserService;
import com.tourly.auth.entity.AccountStatus;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // =========================
    // Get all users
    // =========================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminUserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminUserService.getAllUsers(page, size);
    }

    // =========================
    // Get user by ID
    // =========================
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponse getUserById(@PathVariable Long id) {
        return adminUserService.getUserById(id);
    }

    // =========================
    // Get users by role
    // Example: /api/admin/users/role/PLANNER
    // =========================
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminUserResponse> getUsersByRole(
            @PathVariable String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminUserService.getUsersByRole(roleName, page, size);
    }

    // =========================
    // Get users by status
    // Example: /api/admin/users/status/ACTIVE
    // =========================
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminUserResponse> getUsersByStatus(
            @PathVariable AccountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminUserService.getUsersByStatus(status, page, size);
    }

    // =========================
    // Suspend user
    // =========================
    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponse suspendUser(@PathVariable Long id) {
        return adminUserService.suspendUser(id);
    }

    // =========================
    // Reactivate user
    // =========================
    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponse reactivateUser(@PathVariable Long id) {
        return adminUserService.reactivateUser(id);
    }
}