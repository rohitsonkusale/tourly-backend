package com.tourly.booking.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.payment.entity.Payment;
import com.tourly.payment.entity.PaymentStage;
import com.tourly.payment.enums.PaymentStageStatus;
import com.tourly.payment.repository.PaymentRepository;
import com.tourly.payment.repository.PaymentStageRepository;
import com.tourly.payment.enums.PaymentStatus;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.repository.TripRepository;

@Component
public class BookingExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryScheduler.class);

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStageRepository paymentStageRepository;
    private final TripRepository tripRepository;
    private final BookingExpiryScheduler self;

    public BookingExpiryScheduler(
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            PaymentStageRepository paymentStageRepository,
            TripRepository tripRepository,
            @org.springframework.context.annotation.Lazy BookingExpiryScheduler self) {

        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.paymentStageRepository = paymentStageRepository;
        this.tripRepository = tripRepository;
        this.self = self;
    }

    // Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    public void releaseExpiredBookings() {

        List<Booking> expiredBookings =
                bookingRepository.findExpiredBookings(LocalDateTime.now());

        for (Booking booking : expiredBookings) {
            try {
                self.processExpiredBooking(booking.getId());
            } catch (OptimisticLockingFailureException ex) {
                // Another process (user cancel or payment) already modified this booking — skip gracefully
                log.info("Skipping expired booking (concurrent modification): bookingId={}", booking.getId());
            } catch (Exception ex) {
                log.error("Failed to process expired booking: bookingId={}, error={}", booking.getId(), ex.getMessage());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processExpiredBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return;

        // Safety: skip if already processed
        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.CONFIRMED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            return;
        }

        Trip trip = booking.getTrip();

        if (trip != null) {
            int updatedSeats = trip.getBookedSeats() - booking.getSeatsBooked();
            trip.setBookedSeats(Math.max(updatedSeats, 0));
            tripRepository.save(trip);
        }

        // Mark booking expired/cancelled
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason("Booking expired: Stage 1 payment not completed");
        booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.PENDING);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Cancel all payment stages for this expired booking
        List<PaymentStage> stages = paymentStageRepository.findByBookingIdAndStatusIn(
                booking.getId(),
                List.of(PaymentStageStatus.PENDING, PaymentStageStatus.INVOICE_SENT)
        );
        for (PaymentStage stage : stages) {
            stage.setStatus(PaymentStageStatus.CANCELLED);
        }
        if (!stages.isEmpty()) {
            paymentStageRepository.saveAll(stages);
        }

        // Also mark payment failed if still CREATED
        Payment payment = paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc(booking.getId()).orElse(null);

        if (payment != null && payment.getStatus() == PaymentStatus.CREATED) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Booking expired before payment");
            paymentRepository.save(payment);
        }

        log.info("Booking expired and cancelled: bookingId={}", bookingId);
    }
}
