package com.tourly.booking.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.dto.request.ValidateCouponRequest;
import com.tourly.booking.dto.response.CouponValidationResponse;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.booking.service.CouponService;
import com.tourly.common.entity.Coupon;
import com.tourly.common.entity.CouponUsage;
import com.tourly.common.enums.CouponStatus;
import com.tourly.common.enums.DiscountType;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.repository.CouponRepository;
import com.tourly.common.repository.CouponUsageRepository;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.repository.TripRepository;

@Service
public class CouponServiceImpl implements CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponServiceImpl.class);

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public CouponServiceImpl(CouponRepository couponRepository,
                             CouponUsageRepository couponUsageRepository,
                             TripRepository tripRepository,
                             UserRepository userRepository,
                             BookingRepository bookingRepository) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(ValidateCouponRequest request) {

        String code = request.getCouponCode().trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        // 1. Find coupon
        Optional<Coupon> optCoupon = couponRepository.findByCode(code);
        if (optCoupon.isEmpty()) {
            return CouponValidationResponse.failure("Invalid coupon code");
        }

        Coupon coupon = optCoupon.get();

        // 2. Status check
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            return CouponValidationResponse.failure("This coupon is no longer active");
        }

        // 3. Date validity
        if (now.isBefore(coupon.getValidFrom())) {
            return CouponValidationResponse.failure("This coupon is not yet active");
        }
        if (now.isAfter(coupon.getValidTo())) {
            return CouponValidationResponse.failure("This coupon has expired");
        }

        // 4. Usage limit
        if (coupon.getUsedCount() >= coupon.getMaxUses()) {
            return CouponValidationResponse.failure("This coupon has reached its usage limit");
        }

        // 5. Per-user usage check
        User currentUser = getCurrentUser();
        if (couponUsageRepository.existsByCouponIdAndUserId(coupon.getId(), currentUser.getId())) {
            return CouponValidationResponse.failure("You have already used this coupon");
        }

        // 6. Trip scope check — if coupon is tied to a specific trip
        if (coupon.getTrip() != null && !coupon.getTrip().getId().equals(request.getTripId())) {
            return CouponValidationResponse.failure("This coupon is not valid for this trip");
        }

        // 7. Destination scope check
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (coupon.getDestination() != null && trip.getDestination() != null
                && !coupon.getDestination().getId().equals(trip.getDestination().getId())) {
            return CouponValidationResponse.failure("This coupon is not valid for this destination");
        }

        // 8. Calculate order total & check minimum
        BigDecimal orderTotal = trip.getBasePrice().multiply(BigDecimal.valueOf(request.getSeats()));

        if (coupon.getMinOrderValue() != null
                && coupon.getMinOrderValue().compareTo(BigDecimal.ZERO) > 0
                && orderTotal.compareTo(coupon.getMinOrderValue()) < 0) {
            return CouponValidationResponse.failure(
                    "Minimum order value of ₹" + coupon.getMinOrderValue().setScale(0, RoundingMode.HALF_UP) + " required");
        }

        // 9. Calculate discount
        BigDecimal discountAmount = calculateDiscount(coupon, orderTotal);
        BigDecimal discountedTotal = orderTotal.subtract(discountAmount);

        return CouponValidationResponse.success(
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                discountAmount,
                orderTotal,
                discountedTotal
        );
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal) {
        BigDecimal discount;

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = orderTotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // FLAT discount
            discount = coupon.getDiscountValue();
        }

        // Discount cannot exceed order total
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void recordUsage(Coupon coupon, Long bookingId, Long userId, BigDecimal discountApplied) {
        // Increment usage count
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        // Record in usage table
        CouponUsage usage = new CouponUsage();
        usage.setCoupon(coupon);
        usage.setBooking(bookingRepository.getReferenceById(bookingId));
        usage.setUser(userRepository.getReferenceById(userId));
        usage.setDiscountApplied(discountApplied);
        couponUsageRepository.save(usage);

        log.info("Coupon usage recorded: couponId={}, bookingId={}, userId={}, discount={}",
                coupon.getId(), bookingId, userId, discountApplied);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
