package com.tourly.payment.scheduler;

import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.payment.entity.PaymentStage;
import com.tourly.payment.enums.PaymentStageStatus;
import com.tourly.payment.repository.PaymentStageRepository;
import com.tourly.payment.service.RefundService;
import com.tourly.notification.service.PaymentNotificationService;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.repository.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks for the payment stage lifecycle:
 *
 * 1. Open invoice windows: PENDING → INVOICE_SENT when invoice_open_date arrives.
 * 2. Mark overdue: INVOICE_SENT → OVERDUE when deadline_at passes without payment.
 * 3. Auto-cancel: Cancel bookings that have OVERDUE stages (missed 72-hour window).
 */
@Component
public class PaymentStageScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentStageScheduler.class);

    private final PaymentStageRepository paymentStageRepository;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final PaymentNotificationService paymentNotificationService;
    private final RefundService refundService;

    public PaymentStageScheduler(
            PaymentStageRepository paymentStageRepository,
            BookingRepository bookingRepository,
            TripRepository tripRepository,
            PaymentNotificationService paymentNotificationService,
            RefundService refundService) {
        this.paymentStageRepository = paymentStageRepository;
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.paymentNotificationService = paymentNotificationService;
        this.refundService = refundService;
    }

    // =========================================================================
    // JOB 1: OPEN INVOICE WINDOWS
    // Runs every hour — finds PENDING stages whose invoice_open_date has arrived
    // and transitions them to INVOICE_SENT.
    // =========================================================================
    @Scheduled(cron = "0 0 * * * *") // Every hour at :00
    @Transactional
    public void openInvoiceWindows() {
        LocalDate today = LocalDate.now();

        List<PaymentStage> stagesToInvoice = paymentStageRepository
                .findStagesReadyForInvoice(PaymentStageStatus.PENDING, today);

        if (stagesToInvoice.isEmpty()) {
            return;
        }

        for (PaymentStage stage : stagesToInvoice) {
            stage.setStatus(PaymentStageStatus.INVOICE_SENT);
            stage.setInvoiceSentAt(LocalDateTime.now());

            log.info("Invoice window opened. bookingId={}, stageNumber={}, dueDate={}, deadlineAt={}",
                    stage.getBooking().getId(), stage.getStageNumber(),
                    stage.getDueDate(), stage.getDeadlineAt());

            // Notify traveler — Event 2: Invoice Opened
            Booking booking = stage.getBooking();
            String tripTitle = booking.getTrip() != null ? booking.getTrip().getTitle() : "Trip";
            paymentNotificationService.notifyInvoiceOpened(
                    booking.getTraveler().getId(), booking.getId(),
                    tripTitle, stage.getLabel(), stage.getAmount(), stage.getDueDate());
        }

        paymentStageRepository.saveAll(stagesToInvoice);

        log.info("Opened invoice windows for {} payment stage(s)", stagesToInvoice.size());
    }

    // =========================================================================
    // JOB 2: MARK OVERDUE STAGES
    // Runs every 30 minutes — finds INVOICE_SENT/PENDING stages past deadline_at
    // and marks them OVERDUE.
    // =========================================================================
    @Scheduled(cron = "0 */30 * * * *") // Every 30 minutes
    @Transactional
    public void markOverdueStages() {
        List<PaymentStageStatus> payableStatuses = List.of(
                PaymentStageStatus.PENDING,
                PaymentStageStatus.INVOICE_SENT
        );

        List<PaymentStage> overdueStages = paymentStageRepository.findOverdueStages(payableStatuses);

        if (overdueStages.isEmpty()) {
            return;
        }

        for (PaymentStage stage : overdueStages) {
            stage.setStatus(PaymentStageStatus.OVERDUE);

            log.warn("Payment stage marked OVERDUE. bookingId={}, stageNumber={}, deadlineAt={}",
                    stage.getBooking().getId(), stage.getStageNumber(), stage.getDeadlineAt());

            // Notify traveler — Event 6: Stage Overdue
            Booking booking = stage.getBooking();
            String tripTitle = booking.getTrip() != null ? booking.getTrip().getTitle() : "Trip";
            paymentNotificationService.notifyStageOverdue(
                    booking.getTraveler().getId(), booking.getId(),
                    tripTitle, stage.getLabel(), stage.getAmount());
        }

        paymentStageRepository.saveAll(overdueStages);

        log.info("Marked {} payment stage(s) as OVERDUE", overdueStages.size());
    }

    // =========================================================================
    // JOB 3: AUTO-CANCEL BOOKINGS WITH OVERDUE STAGES
    // Runs every hour — cancels bookings that have overdue stages.
    // Per policy: failure to pay within 72-hour window → automatic cancellation.
    // =========================================================================
    @Scheduled(cron = "0 15 * * * *") // Every hour at :15 (offset from Job 1 & 2)
    @Transactional
    public void autoCancelOverdueBookings() {

        // Find all stages currently marked OVERDUE
        var overdueStages = paymentStageRepository.findByStatus(PaymentStageStatus.OVERDUE);

        if (overdueStages.isEmpty()) {
            return;
        }

        // Get distinct booking IDs with overdue stages
        var bookingIds = overdueStages.stream()
                .map(s -> s.getBooking().getId())
                .distinct()
                .toList();

        int cancelledCount = 0;

        for (Long bookingId : bookingIds) {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);

            if (booking == null || booking.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }

            // Cancel the booking
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason("Auto-cancelled: payment deadline missed");
            booking.setCancelledAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            // Release seats back to trip
            Trip trip = booking.getTrip();
            if (trip != null && booking.getSeatsBooked() != null) {
                int newBookedSeats = Math.max(0, trip.getBookedSeats() - booking.getSeatsBooked());
                trip.setBookedSeats(newBookedSeats);
                tripRepository.save(trip);
                log.info("Released {} seat(s) back to tripId={}", booking.getSeatsBooked(), trip.getId());
            }

            // Cancel all unpaid stages for this booking
            List<PaymentStage> unpaidStages = paymentStageRepository
                    .findByBookingIdAndStatusIn(bookingId, List.of(
                            PaymentStageStatus.PENDING,
                            PaymentStageStatus.INVOICE_SENT,
                            PaymentStageStatus.OVERDUE
                    ));

            for (PaymentStage stage : unpaidStages) {
                stage.setStatus(PaymentStageStatus.CANCELLED);
            }
            paymentStageRepository.saveAll(unpaidStages);

            cancelledCount++;
            log.warn("Booking auto-cancelled due to missed payment. bookingId={}", bookingId);

            // Initiate refund for previously paid stages per cancellation policy
            refundService.initiateAutoCancellationRefund(bookingId, "Auto-cancelled: payment deadline missed");

            // Notify traveler — Event 7: Auto-Cancellation
            String tripTitle = booking.getTrip() != null ? booking.getTrip().getTitle() : "Trip";
            paymentNotificationService.notifyAutoCancellation(
                    booking.getTraveler().getId(), bookingId, tripTitle);

            // Notify host
            var host = booking.getTrip() != null ? booking.getTrip().getHost() : null;
            if (host == null && booking.getTrip() != null) host = booking.getTrip().getPlanner();
            if (host != null) {
                paymentNotificationService.notifyHostAutoCancellation(
                        host.getId(), bookingId,
                        booking.getTraveler().getFullName(), tripTitle);
            }
        }

        if (cancelledCount > 0) {
            log.info("Auto-cancelled {} booking(s) due to overdue payment stages", cancelledCount);
        }
    }

    // =========================================================================
    // JOB 4: SEND 24H DEADLINE REMINDERS
    // Runs every hour — finds INVOICE_SENT stages whose deadline is within 24h
    // and sends a reminder notification (if not already sent for that stage).
    // =========================================================================
    @Scheduled(cron = "0 45 * * * *") // Every hour at :45
    @Transactional
    public void sendDeadlineReminders() {
        LocalDateTime now = LocalDateTime.now();

        // Find stages that are INVOICE_SENT with deadline in the next 24 hours
        // Only pick stages whose deadline is between 23–24 hours from now
        // to avoid sending duplicate reminders each hour
        LocalDateTime windowStart = now.plusHours(23);
        LocalDateTime windowEnd = now.plusHours(24);

        var invoiceSentStages = paymentStageRepository.findAll().stream()
                .filter(s -> s.getStatus() == PaymentStageStatus.INVOICE_SENT)
                .filter(s -> s.getDeadlineAt() != null)
                .filter(s -> s.getDeadlineAt().isAfter(windowStart) && s.getDeadlineAt().isBefore(windowEnd))
                .toList();

        if (invoiceSentStages.isEmpty()) {
            return;
        }

        for (PaymentStage stage : invoiceSentStages) {
            Booking booking = stage.getBooking();
            String tripTitle = booking.getTrip() != null ? booking.getTrip().getTitle() : "Trip";

            // Event 3: 24h Deadline Reminder
            paymentNotificationService.notifyDeadlineReminder(
                    booking.getTraveler().getId(), booking.getId(),
                    tripTitle, stage.getLabel(), stage.getAmount());
        }

        log.info("Sent 24h deadline reminders for {} stage(s)", invoiceSentStages.size());
    }
}
