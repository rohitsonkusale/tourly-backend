package com.tourly.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourly.auth.entity.Role;
import com.tourly.auth.entity.RolePermission;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(Role role);
}