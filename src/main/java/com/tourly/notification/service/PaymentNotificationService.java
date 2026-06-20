package com.tourly.notification.service;

import com.tourly.notification.enums.NotificationTargetType;
import com.tourly.notification.enums.NotificationType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Specialized notification helper for payment stage lifecycle events.
 * Generates proper titles and messages for each of the 7 payment events.
 */
@Service
public class PaymentNotificationService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private final NotificationService notificationService;

    public PaymentNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // =========================================================================
    // EVENT 1: BOOKING CREATED — Traveler needs to pay Stage 1
    // =========================================================================
    public void notifyBookingCreated(Long travelerId, Long bookingId, String tripTitle, BigDecimal depositAmount) {
        String title = "Booking Confirmed — Pay Deposit";
        String message = String.format(
                "Your booking for \"%s\" is reserved! Pay %s now to secure your spot. " +
                "The booking will expire if payment is not completed within 10 minutes.",
                tripTitle, formatCurrency(depositAmount));

        notificationService.send(travelerId, title, message,
                NotificationType.PAYMENT, NotificationTargetType.BOOKING, bookingId);
    }

    // =========================================================================
    // EVENT 2: INVOICE WINDOW OPENED — Stage moved to INVOICE_SENT
    // =========================================================================
    public void notifyInvoiceOpened(Long travelerId, Long bookingId, String tripTitle,
                                    String stageLabel, BigDecimal amount, LocalDate dueDate) {
        String title = "Payment Due — " + stageLabel;
        String message = String.format(
                "%s of %s is now due for \"%s\". Please pay by %s to keep your booking active.",
                stageLabel, formatCurrency(amount), tripTitle, dueDate.format(DATE_FMT));

        notificationService.send(travelerId, title, message,
                NotificationType.PAYMENT, NotificationTargetType.BOOKING, bookingId);
    }

    // =========================================================================
    // EVENT 3: 24H REMINDER — Deadline approaching
    // =========================================================================
    public void notifyDeadlineReminder(Long travelerId, Long bookingId, String tripTitle,
                                       String stageLabel, BigDecimal amount) {
        String title = "⏰ Payment Due Tomorrow";
        String message = String.format(
                "Reminder: %s of %s for \"%s\" is due within 24 hours. " +
                "Pay now to avoid automatic cancellation.",
                stageLabel, formatCurrency(amount), tripTitle);

        notificationService.send(travelerId, title, message,
                NotificationType.PAYMENT, NotificationTargetType.BOOKING, bookingId);
    }

    // =========================================================================
    // EVENT 4: PAYMENT SUCCESSFUL — Stage paid
    // =========================================================================
    public void notifyPaymentSuccess(Long travelerId, Long bookingId, String tripTitle,
                                     String stageLabel, BigDecimal amount) {
        String title = "Payment Received ✓";
        String message = String.format(
                "%s of %s paid successfully for \"%s\". Thank you!",
                stageLabel, formatCurrency(amount), tripTitle);

        notificationService.send(travelerId, title, message,
                NotificationType.PAYMENT, NotificationTargetType.BOOKING, bookingId);
    }

    public void notifyHostPaymentReceived(Long hostId, Long bookingId, String travelerName,
                                          String tripTitle, String stageLabel, BigDecimal amount) {
        String title = "Payment Received from Traveler";
        String message = String.format(
                "%s paid %s (%s) for \"%s\".",
                travelerName, formatCurrency(amount), stageLabel, tripTitle);

        notificationService.send(hostId, title, message,
                NotificationType.PAYMENT, NotificationTargetType.BOOKING, bookingId);
    }

    // =========================================================================
    // EVENT 5: PAYMENT FAILED
    // =========================================================================
    public void notifyPaymentFailed(Long travelerId, Long bookingId, String tripTitle,
                                    String stageLabel, BigDecimal amount) {
        String title = "Payment Failed";
        String message = String.format(
                "Your payment of %s for %s (\"%s\") could not be processed. " +
                "Please retry before the deadline to keep your booking.",
                formatCurrency(amount), stageLabel, tripTitle);

        notificationService.send(travelerId, title, message,
                NotificationType.PAYMENT, NotificationTargetType.BOOKING, bookingId);
    }

    // =========================================================================
    // EVENT 6: STAGE OVERDUE — Deadline missed
    // =========================================================================
    public void notifyStageOverdue(Long travelerId, Long bookingId, String tripTitle,
                                   String stageLabel, BigDecimal amount) {
        String title = "⚠️ Payment Deadline Missed";
        String message = String.format(
                "The payment deadline for %s (%s) for \"%s\" has passed. " +
                "Your booking may be cancelled automatically.",
                stageLabel, formatCurrency(amount), tripTitle);

        notificationService.send(travelerId, title, message,
                NotificationType.PAYMENT, NotificationTargetType.BOOKING, bookingId);
    }

    // =========================================================================
    // EVENT 7: AUTO-CANCELLATION — Booking cancelled due to missed payment
    // =========================================================================
    public void notifyAutoCancellation(Long travelerId, Long bookingId, String tripTitle) {
        String title = "Booking Cancelled";
        String message = String.format(
                "Your booking for \"%s\" has been cancelled due to a missed payment deadline. " +
                "If you previously paid any stages, a refund will be processed per our policy.",
                tripTitle);

        notificationService.send(travelerId, title, message,
                NotificationType.BOOKING, NotificationTargetType.BOOKING, bookingId);
    }

    public void notifyHostAutoCancellation(Long hostId, Long bookingId, String travelerName, String tripTitle) {
        String title = "Booking Auto-Cancelled";
        String message = String.format(
                "Booking by %s for \"%s\" was automatically cancelled due to missed payment. " +
                "The seat has been released.",
                travelerName, tripTitle);

        notificationService.send(hostId, title, message,
                NotificationType.BOOKING, NotificationTargetType.BOOKING, bookingId);
    }

    // =========================================================================
    // UTILITY
    // =========================================================================
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0";
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
        fmt.setMaximumFractionDigits(0);
        return fmt.format(amount);
    }
}
