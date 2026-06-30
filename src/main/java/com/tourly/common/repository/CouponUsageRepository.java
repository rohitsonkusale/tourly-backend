package com.tourly.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourly.common.entity.CouponUsage;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    boolean existsByCouponIdAndUserId(Long couponId, Long userId);

    long countByCouponId(Long couponId);
}
