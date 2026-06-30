package com.tourly.admin.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.admin.dto.request.CreateCouponRequest;
import com.tourly.admin.dto.response.AdminCouponResponse;
import com.tourly.admin.service.AdminCouponService;
import com.tourly.common.entity.Coupon;
import com.tourly.common.enums.CouponStatus;
import com.tourly.common.enums.DiscountType;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ConflictException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.repository.CouponRepository;
import com.tourly.trip.entity.Destination;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.repository.DestinationRepository;
import com.tourly.trip.repository.TripRepository;

import java.math.BigDecimal;

@Service
public class AdminCouponServiceImpl implements AdminCouponService {

    private final CouponRepository couponRepository;
    private final TripRepository tripRepository;
    private final DestinationRepository destinationRepository;

    public AdminCouponServiceImpl(CouponRepository couponRepository,
                                   TripRepository tripRepository,
                                   DestinationRepository destinationRepository) {
        this.couponRepository = couponRepository;
        this.tripRepository = tripRepository;
        this.destinationRepository = destinationRepository;
    }

    @Override
    @Transactional
    public AdminCouponResponse createCoupon(CreateCouponRequest request) {
        String code = request.getCode().trim().toUpperCase();

        if (couponRepository.existsByCode(code)) {
            throw new ConflictException("Coupon code already exists: " + code);
        }

        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new BadRequestException("Valid-to date must be after valid-from date");
        }

        DiscountType discountType;
        try {
            discountType = DiscountType.valueOf(request.getDiscountType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid discount type. Must be PERCENTAGE or FLAT");
        }

        if (discountType == DiscountType.PERCENTAGE && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Percentage discount cannot exceed 100%");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxUses(request.getMaxUses());
        coupon.setMinOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : BigDecimal.ZERO);
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidTo(request.getValidTo());
        coupon.setStatus(CouponStatus.ACTIVE);

        if (request.getTripId() != null) {
            Trip trip = tripRepository.findById(request.getTripId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
            coupon.setTrip(trip);
        }

        if (request.getDestinationId() != null) {
            Destination destination = destinationRepository.findById(request.getDestinationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination not found"));
            coupon.setDestination(destination);
        }

        Coupon saved = couponRepository.save(coupon);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCouponResponse> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        return mapToResponse(coupon);
    }

    @Override
    @Transactional
    public AdminCouponResponse updateCouponStatus(Long id, String status) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        CouponStatus newStatus;
        try {
            newStatus = CouponStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Must be ACTIVE, PAUSED, or EXPIRED");
        }

        coupon.setStatus(newStatus);
        Coupon saved = couponRepository.save(coupon);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        if (coupon.getUsedCount() > 0) {
            throw new BadRequestException("Cannot delete a coupon that has been used. Pause it instead.");
        }

        couponRepository.delete(coupon);
    }

    private AdminCouponResponse mapToResponse(Coupon coupon) {
        AdminCouponResponse r = new AdminCouponResponse();
        r.setId(coupon.getId());
        r.setCode(coupon.getCode());
        r.setDiscountType(coupon.getDiscountType().name());
        r.setDiscountValue(coupon.getDiscountValue());
        r.setMaxUses(coupon.getMaxUses());
        r.setUsedCount(coupon.getUsedCount());
        r.setMinOrderValue(coupon.getMinOrderValue());
        r.setStatus(coupon.getStatus().name());
        r.setValidFrom(coupon.getValidFrom());
        r.setValidTo(coupon.getValidTo());
        r.setCreatedAt(coupon.getCreatedAt());

        if (coupon.getTrip() != null) {
            r.setTripId(coupon.getTrip().getId());
            r.setTripTitle(coupon.getTrip().getTitle());
        }
        if (coupon.getDestination() != null) {
            r.setDestinationId(coupon.getDestination().getId());
            r.setDestinationName(coupon.getDestination().getCity());
        }

        return r;
    }
}
