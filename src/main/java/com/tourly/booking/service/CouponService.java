package com.tourly.booking.service;

import java.math.BigDecimal;

import com.tourly.booking.dto.request.ValidateCouponRequest;
import com.tourly.booking.dto.response.CouponValidationResponse;
import com.tourly.common.entity.Coupon;

public interface CouponService {

    /**
     * Validate a coupon code against a trip + seat count.
     * Returns discount details if valid, or failure reason.
     */
    CouponValidationResponse validateCoupon(ValidateCouponRequest request);

    /**
     * Calculate the discount amount for a given coupon and order total.
     */
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal);

    /**
     * Record coupon usage after a booking is confirmed.
     */
    void recordUsage(Coupon coupon, Long bookingId, Long userId, BigDecimal discountApplied);
}
