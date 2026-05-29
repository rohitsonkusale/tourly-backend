package com.tourly.auth.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    Page<User> findByDeletedDateIsNull(Pageable pageable);

    Page<User> findByDeletedDateIsNotNull(Pageable pageable);

    Page<User> findByAccountStatusAndDeletedDateIsNull(AccountStatus accountStatus, Pageable pageable);

    Page<User> findByRole_NameAndDeletedDateIsNull(RoleName roleName, Pageable pageable);

    Optional<User> findByIdAndDeletedDateIsNull(Long id);
}