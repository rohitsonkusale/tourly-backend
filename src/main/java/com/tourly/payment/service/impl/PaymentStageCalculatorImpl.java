package com.tourly.payment.service.impl;

import com.tourly.payment.entity.PaymentStage;
import com.tourly.payment.enums.PaymentStageStatus;
import com.tourly.payment.enums.ScheduleType;
import com.tourly.payment.service.PaymentStageCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the dynamic payment stage calculation engine.
 *
 * Policy Summary:
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │ Booking Window          │ Stages │ Split          │ Due Dates               │
 * ├─────────────────────────┼────────┼────────────────┼─────────────────────────┤
 * │ 30+ days before         │ 3      │ 30% / 40% / 30│ Now / D-15 / D-7        │
 * │ 15–29 days before       │ 2      │ 40% / 60%     │ Now / D-7               │
 * │ 7–14 days before        │ 1      │ 100%          │ Now                     │
 * │ Less than 7 days        │ ❌     │ —             │ Booking closed          │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * Invoice Windows:
 * - 3-Stage: Stage 2 opens D-18, due D-15. Stage 3 opens D-10, due D-7.
 * - 2-Stage (18–29 days out): Stage 2 opens D-10, due D-7.
 * - 2-Stage Overlap (15–17 days out): Stage 2 opens immediately, due within 72h.
 */
@Service
public class PaymentStageCalculatorImpl implements PaymentStageCalculator {

    // Schedule thresholds (days before departure)
    private static final long THREE_STAGE_MIN_DAYS = 30;
    private static final long TWO_STAGE_MIN_DAYS = 15;
    private static final long BOOKING_MIN_DAYS = 7;

    // Overlap zone: bookings made 15–17 days out get immediate Stage 2
    private static final long OVERLAP_THRESHOLD_DAYS = 18;

    // Percentage splits
    private static final BigDecimal THREE_STAGE_1_PCT = new BigDecimal("30.00");
    private static final BigDecimal THREE_STAGE_2_PCT = new BigDecimal("40.00");
    private static final BigDecimal THREE_STAGE_3_PCT = new BigDecimal("30.00");

    private static final BigDecimal TWO_STAGE_1_PCT = new BigDecimal("40.00");
    private static final BigDecimal TWO_STAGE_2_PCT = new BigDecimal("60.00");

    private static final BigDecimal FULL_PAYMENT_PCT = new BigDecimal("100.00");

    // 72-hour deadline from invoice open
    private static final int DEADLINE_HOURS = 72;

    @Override
    public boolean isBookingAllowed(LocalDate bookingDate, LocalDate departureDate) {
        long daysUntilDeparture = ChronoUnit.DAYS.between(bookingDate, departureDate);
        return daysUntilDeparture >= BOOKING_MIN_DAYS;
    }

    @Override
    public ScheduleType determineScheduleType(LocalDate bookingDate, LocalDate departureDate) {
        long daysUntilDeparture = ChronoUnit.DAYS.between(bookingDate, departureDate);

        if (daysUntilDeparture < BOOKING_MIN_DAYS) {
            throw new IllegalArgumentException(
                "Booking not allowed: departure is only " + daysUntilDeparture +
                " days away. Minimum required: " + BOOKING_MIN_DAYS + " days."
            );
        }

        if (daysUntilDeparture >= THREE_STAGE_MIN_DAYS) {
            return ScheduleType.THREE_STAGE;
        } else if (daysUntilDeparture >= TWO_STAGE_MIN_DAYS) {
            return ScheduleType.TWO_STAGE;
        } else {
            return ScheduleType.FULL_PAYMENT;
        }
    }

    @Override
    public List<PaymentStage> generateStages(LocalDate bookingDate, LocalDate departureDate, BigDecimal totalPrice) {
        ScheduleType scheduleType = determineScheduleType(bookingDate, departureDate);

        return switch (scheduleType) {
            case THREE_STAGE -> buildThreeStageSchedule(bookingDate, departureDate, totalPrice);
            case TWO_STAGE -> buildTwoStageSchedule(bookingDate, departureDate, totalPrice);
            case FULL_PAYMENT -> buildFullPaymentSchedule(bookingDate, totalPrice);
        };
    }

    // =========================================================================
    // 3-STAGE SCHEDULE (30+ days before departure)
    // Stage 1: 30% — due immediately
    // Stage 2: 40% — invoice opens D-18, due D-15
    // Stage 3: 30% — invoice opens D-10, due D-7
    // =========================================================================
    private List<PaymentStage> buildThreeStageSchedule(
            LocalDate bookingDate, LocalDate departureDate, BigDecimal totalPrice) {

        List<PaymentStage> stages = new ArrayList<>();

        BigDecimal stage1Amount = calculateAmount(totalPrice, THREE_STAGE_1_PCT);
        BigDecimal stage2Amount = calculateAmount(totalPrice, THREE_STAGE_2_PCT);
        // Stage 3 absorbs rounding remainder
        BigDecimal stage3Amount = totalPrice.subtract(stage1Amount).subtract(stage2Amount);

        // Stage 1: Booking Deposit — immediate
        PaymentStage stage1 = new PaymentStage();
        stage1.setStageNumber(1);
        stage1.setLabel("Booking Deposit");
        stage1.setAmount(stage1Amount);
        stage1.setPercentage(THREE_STAGE_1_PCT);
        stage1.setStatus(PaymentStageStatus.PENDING);
        stage1.setDueDate(bookingDate);
        stage1.setInvoiceOpenDate(bookingDate);
        // Stage 1 deadline is managed by booking.expiresAt (10 min), not this field
        stage1.setDeadlineAt(null);
        stage1.setIsImmediate(true);
        stages.add(stage1);

        // Stage 2: Second Installment — invoice opens D-18, due D-15
        LocalDate stage2DueDate = departureDate.minusDays(15);
        LocalDate stage2InvoiceOpen = departureDate.minusDays(18);
        LocalDateTime stage2Deadline = stage2InvoiceOpen.atStartOfDay().plusHours(DEADLINE_HOURS);

        PaymentStage stage2 = new PaymentStage();
        stage2.setStageNumber(2);
        stage2.setLabel("Second Installment");
        stage2.setAmount(stage2Amount);
        stage2.setPercentage(THREE_STAGE_2_PCT);
        stage2.setStatus(PaymentStageStatus.PENDING);
        stage2.setDueDate(stage2DueDate);
        stage2.setInvoiceOpenDate(stage2InvoiceOpen);
        stage2.setDeadlineAt(stage2Deadline);
        stage2.setIsImmediate(false);
        stages.add(stage2);

        // Stage 3: Final Payment — invoice opens D-10, due D-7
        LocalDate stage3DueDate = departureDate.minusDays(7);
        LocalDate stage3InvoiceOpen = departureDate.minusDays(10);
        LocalDateTime stage3Deadline = stage3InvoiceOpen.atStartOfDay().plusHours(DEADLINE_HOURS);

        PaymentStage stage3 = new PaymentStage();
        stage3.setStageNumber(3);
        stage3.setLabel("Final Payment");
        stage3.setAmount(stage3Amount);
        stage3.setPercentage(THREE_STAGE_3_PCT);
        stage3.setStatus(PaymentStageStatus.PENDING);
        stage3.setDueDate(stage3DueDate);
        stage3.setInvoiceOpenDate(stage3InvoiceOpen);
        stage3.setDeadlineAt(stage3Deadline);
        stage3.setIsImmediate(false);
        stages.add(stage3);

        return stages;
    }

    // =========================================================================
    // 2-STAGE SCHEDULE (15–29 days before departure)
    // Stage 1: 40% — due immediately
    // Stage 2: 60% — depends on overlap zone:
    //   Standard (18–29 days out): invoice opens D-10, due D-7
    //   Overlap (15–17 days out): invoice opens immediately, due within 72h
    // =========================================================================
    private List<PaymentStage> buildTwoStageSchedule(
            LocalDate bookingDate, LocalDate departureDate, BigDecimal totalPrice) {

        List<PaymentStage> stages = new ArrayList<>();
        long daysUntilDeparture = ChronoUnit.DAYS.between(bookingDate, departureDate);
        boolean isOverlapZone = daysUntilDeparture < OVERLAP_THRESHOLD_DAYS;

        BigDecimal stage1Amount = calculateAmount(totalPrice, TWO_STAGE_1_PCT);
        // Stage 2 absorbs rounding remainder
        BigDecimal stage2Amount = totalPrice.subtract(stage1Amount);

        // Stage 1: Booking Deposit — immediate
        PaymentStage stage1 = new PaymentStage();
        stage1.setStageNumber(1);
        stage1.setLabel("Booking Deposit");
        stage1.setAmount(stage1Amount);
        stage1.setPercentage(TWO_STAGE_1_PCT);
        stage1.setStatus(PaymentStageStatus.PENDING);
        stage1.setDueDate(bookingDate);
        stage1.setInvoiceOpenDate(bookingDate);
        // Stage 1 deadline is managed by booking.expiresAt (10 min), not this field
        stage1.setDeadlineAt(null);
        stage1.setIsImmediate(true);
        stages.add(stage1);

        // Stage 2: Final Payment
        PaymentStage stage2 = new PaymentStage();
        stage2.setStageNumber(2);
        stage2.setLabel("Final Payment");
        stage2.setAmount(stage2Amount);
        stage2.setPercentage(TWO_STAGE_2_PCT);
        stage2.setStatus(PaymentStageStatus.PENDING);

        if (isOverlapZone) {
            // Overlap: 15–17 days out — invoice opens immediately, 72h from now to pay
            stage2.setDueDate(bookingDate.plusDays(3));
            stage2.setInvoiceOpenDate(bookingDate);
            stage2.setDeadlineAt(LocalDateTime.now().plusHours(DEADLINE_HOURS));
            stage2.setIsImmediate(true);
        } else {
            // Standard: 18–29 days out — invoice opens D-10, due D-7
            LocalDate stage2DueDate = departureDate.minusDays(7);
            LocalDate stage2InvoiceOpen = departureDate.minusDays(10);
            LocalDateTime stage2Deadline = stage2InvoiceOpen.atStartOfDay().plusHours(DEADLINE_HOURS);

            stage2.setDueDate(stage2DueDate);
            stage2.setInvoiceOpenDate(stage2InvoiceOpen);
            stage2.setDeadlineAt(stage2Deadline);
            stage2.setIsImmediate(false);
        }

        stages.add(stage2);
        return stages;
    }

    // =========================================================================
    // 1-STAGE FULL PAYMENT (7–14 days before departure)
    // Stage 1: 100% — due immediately
    // =========================================================================
    private List<PaymentStage> buildFullPaymentSchedule(LocalDate bookingDate, BigDecimal totalPrice) {

        List<PaymentStage> stages = new ArrayList<>();

        PaymentStage stage1 = new PaymentStage();
        stage1.setStageNumber(1);
        stage1.setLabel("Full Payment");
        stage1.setAmount(totalPrice);
        stage1.setPercentage(FULL_PAYMENT_PCT);
        stage1.setStatus(PaymentStageStatus.PENDING);
        stage1.setDueDate(bookingDate);
        stage1.setInvoiceOpenDate(bookingDate);
        // Stage 1 deadline is managed by booking.expiresAt (10 min), not this field
        stage1.setDeadlineAt(null);
        stage1.setIsImmediate(true);
        stages.add(stage1);

        return stages;
    }

    // =========================================================================
    // UTILITY
    // =========================================================================

    /**
     * Calculates a percentage of the total, rounded to 2 decimal places (HALF_UP).
     * The last stage should use subtraction to absorb rounding remainders.
     */
    private BigDecimal calculateAmount(BigDecimal totalPrice, BigDecimal percentage) {
        return totalPrice
                .multiply(percentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
