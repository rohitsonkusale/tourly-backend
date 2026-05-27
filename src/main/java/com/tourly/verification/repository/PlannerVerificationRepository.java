package com.tourly.verification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourly.verification.entity.PlannerVerification;
import com.tourly.verification.enums.VerificationStatus;

@Repository
public interface PlannerVerificationRepository extends JpaRepository<PlannerVerification, Long> {

    Optional<PlannerVerification> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    long countByVerificationStatus(VerificationStatus verificationStatus);

    List<PlannerVerification> findByVerificationStatusOrderBySubmittedAtAsc(VerificationStatus verificationStatus);
}