package com.tourly.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tourly.auth.entity.AccountStatus;
import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByGoogleId(String googleId);
    Boolean existsByEmail(String email);
    Boolean existsByPhone(String phone);

    // =========================
    // Admin User Management
    // =========================
    Page<User> findByDeletedAtIsNull(Pageable pageable);

    Page<User> findByDeletedAtIsNotNull(Pageable pageable);

    Page<User> findByAccountStatusAndDeletedAtIsNull(AccountStatus accountStatus, Pageable pageable);

    Page<User> findByRole_NameAndDeletedAtIsNull(RoleName roleName, Pageable pageable);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    // =========================
    // Pending Approval Queries
    // =========================
    @Query(value = "SELECT * FROM users WHERE role_id = :roleId AND (admin_approval_flag = 'N' OR admin_approval_flag IS NULL) AND deleted_at IS NULL", nativeQuery = true)
    List<User> findPendingApprovalsByRoleId(@Param("roleId") int roleId);

    // =========================
    // Admin Dashboard Stats
    // =========================
    long countByRole_NameAndAccountStatusAndDeletedAtIsNull(RoleName roleName, AccountStatus accountStatus);

    long countByAccountStatus(AccountStatus accountStatus);

    // =========================
    // Founding Host Check (first 10 hosts by registration order)
    // =========================
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = 'HOST' AND u.deletedAt IS NULL AND u.createdAt < :hostCreatedAt")
    long countHostsRegisteredBefore(@Param("hostCreatedAt") java.time.LocalDateTime hostCreatedAt);
}
