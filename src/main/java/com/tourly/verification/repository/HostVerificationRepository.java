package com.tourly.verification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourly.common.entity.HostVerification;

@Repository
public interface HostVerificationRepository extends JpaRepository<HostVerification, Long> {

    Optional<HostVerification> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
