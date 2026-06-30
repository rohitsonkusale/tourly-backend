package com.tourly.common.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tourly.common.entity.Coupon;
import com.tourly.common.enums.CouponStatus;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.status = :status " +
           "AND c.validFrom <= :now AND c.validTo >= :now")
    Optional<Coupon> findActiveByCode(@Param("code") String code,
                                      @Param("status") CouponStatus status,
                                      @Param("now") LocalDateTime now);

    Page<Coupon> findByStatus(CouponStatus status, Pageable pageable);

    boolean existsByCode(String code);
}
