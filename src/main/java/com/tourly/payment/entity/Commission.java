package com.tourly.payment.entity;

import com.tourly.booking.entity.Booking;
import com.tourly.payment.enums.CommissionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commissions")
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "booking_amount", nullable = false)
    private BigDecimal bookingAmount;

    @Column(name = "commission_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", nullable = false)
    private BigDecimal commissionAmount;

    @Column(name = "tax_on_commission")
    private BigDecimal taxOnCommission = BigDecimal.ZERO;

    @Column(name = "net_platform_earning", nullable = false)
    private BigDecimal netPlatformEarning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CommissionStatus status;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt = LocalDateTime.now();

    public Commission() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public BigDecimal getBookingAmount() {
        return bookingAmount;
    }

    public void setBookingAmount(BigDecimal bookingAmount) {
        this.bookingAmount = bookingAmount;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public BigDecimal getTaxOnCommission() {
        return taxOnCommission;
    }

    public void setTaxOnCommission(BigDecimal taxOnCommission) {
        this.taxOnCommission = taxOnCommission;
    }

    public BigDecimal getNetPlatformEarning() {
        return netPlatformEarning;
    }

    public void setNetPlatformEarning(BigDecimal netPlatformEarning) {
        this.netPlatformEarning = netPlatformEarning;
    }

    public CommissionStatus getStatus() {
        return status;
    }

    public void setStatus(CommissionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
