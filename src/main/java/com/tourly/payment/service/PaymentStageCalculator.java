package com.tourly.payment.service;

import com.tourly.payment.entity.PaymentStage;
import com.tourly.payment.enums.ScheduleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Calculates and generates payment stages based on Roamaya's dynamic payment schedule policy.
 *
 * Rules:
 * - 30+ days before departure  → 3 stages (30% / 40% / 30%)
 * - 15–29 days before departure → 2 stages (40% / 60%)
 * - 7–14 days before departure  → 1 stage  (100% upfront)
 * - Less than 7 days            → Booking not allowed (must be rejected upstream)
 */
public interface PaymentStageCalculator {

    /**
     * Determines which schedule type applies based on booking date and departure date.
     *
     * @param bookingDate   the date the booking is being made
     * @param departureDate the trip departure date
     * @return the applicable ScheduleType
     * @throws IllegalArgumentException if departure is less than 7 days away
     */
    ScheduleType determineScheduleType(LocalDate bookingDate, LocalDate departureDate);

    /**
     * Generates the full list of payment stages for a booking.
     * Does NOT persist — caller is responsible for saving.
     *
     * @param bookingDate   the date the booking is being made
     * @param departureDate the trip departure date
     * @param totalPrice    the total trip price after discounts/taxes
     * @return ordered list of PaymentStage entities (not yet persisted)
     */
    List<PaymentStage> generateStages(LocalDate bookingDate, LocalDate departureDate, BigDecimal totalPrice);

    /**
     * Validates whether a booking can be made for the given dates.
     * Returns false if departure is less than 7 days from booking date.
     *
     * @param bookingDate   the date the booking is being made
     * @param departureDate the trip departure date
     * @return true if booking is allowed, false otherwise
     */
    boolean isBookingAllowed(LocalDate bookingDate, LocalDate departureDate);
}
