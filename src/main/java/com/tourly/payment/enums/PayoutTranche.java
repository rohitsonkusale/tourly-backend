package com.tourly.payment.enums;

/**
 * Represents the payout tranche type in the 3-payout model:
 * 
 * ADVANCE_1: Released 30 days before departure for bookings confirmed ≥30 days out.
 * ADVANCE_2: Released 15 days before departure for bookings confirmed between 30-15 days out.
 * FINAL: Released 48 hours post-departure for all remaining balance minus commission.
 */
public enum PayoutTranche {
    ADVANCE_1,
    ADVANCE_2,
    FINAL
}
