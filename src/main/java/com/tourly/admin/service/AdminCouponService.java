package com.tourly.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tourly.admin.dto.request.CreateCouponRequest;
import com.tourly.admin.dto.response.AdminCouponResponse;

public interface AdminCouponService {

    AdminCouponResponse createCoupon(CreateCouponRequest request);

    Page<AdminCouponResponse> getAllCoupons(Pageable pageable);

    AdminCouponResponse getCouponById(Long id);

    AdminCouponResponse updateCouponStatus(Long id, String status);

    void deleteCoupon(Long id);
}
