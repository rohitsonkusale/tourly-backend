package com.tourly.booking.dto.response;

import java.math.BigDecimal;

public class CouponValidationResponse {

    private boolean valid;
    private String message;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal originalTotal;
    private BigDecimal discountedTotal;

    public CouponValidationResponse() {}

    public static CouponValidationResponse success(String discountType, BigDecimal discountValue,
                                                    BigDecimal discountAmount, BigDecimal originalTotal,
                                                    BigDecimal discountedTotal) {
        CouponValidationResponse r = new CouponValidationResponse();
        r.valid = true;
        r.message = "Coupon applied successfully";
        r.discountType = discountType;
        r.discountValue = discountValue;
        r.discountAmount = discountAmount;
        r.originalTotal = originalTotal;
        r.discountedTotal = discountedTotal;
        return r;
    }

    public static CouponValidationResponse failure(String message) {
        CouponValidationResponse r = new CouponValidationResponse();
        r.valid = false;
        r.message = message;
        return r;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getOriginalTotal() { return originalTotal; }
    public void setOriginalTotal(BigDecimal originalTotal) { this.originalTotal = originalTotal; }
    public BigDecimal getDiscountedTotal() { return discountedTotal; }
    public void setDiscountedTotal(BigDecimal discountedTotal) { this.discountedTotal = discountedTotal; }
}
