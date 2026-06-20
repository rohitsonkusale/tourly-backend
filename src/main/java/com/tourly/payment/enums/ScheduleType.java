package com.tourly.payment.enums;

/**
 * Determines the payment schedule for a booking based on
 * how many days before departure the booking was made.
 *
 * THREE_STAGE  → Booked 30+ days before departure (30% / 40% / 30%)
 * TWO_STAGE    → Booked 15–29 days before departure (40% / 60%)
 * FULL_PAYMENT → Booked 7–14 days before departure (100% upfront)
 */
public enum ScheduleType {
    THREE_STAGE,
    TWO_STAGE,
    FULL_PAYMENT
}
