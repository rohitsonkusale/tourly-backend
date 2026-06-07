package com.tourly.verification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tourly.common.entity.HostVerification;
import com.tourly.verification.enums.VerificationStatus;

@Repository
public interface HostVerificationRepository extends JpaRepository<HostVerification, Long> {

    Optional<HostVerification> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<HostVerification> findByVerificationStatusOrderBySubmittedAtAsc(VerificationStatus status);

    @Query("SELECT h FROM HostVerification h JOIN FETCH h.user WHERE h.id = :id")
    Optional<HostVerification> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT h FROM HostVerification h JOIN FETCH h.user WHERE h.verificationStatus = :status ORDER BY h.submittedAt ASC")
    List<HostVerification> findByStatusWithUser(@Param("status") VerificationStatus status);
}
