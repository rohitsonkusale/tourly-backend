package com.tourly.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourly.auth.entity.Permission;
import com.tourly.auth.entity.PermissionName;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermissionName(PermissionName permissionName);
}