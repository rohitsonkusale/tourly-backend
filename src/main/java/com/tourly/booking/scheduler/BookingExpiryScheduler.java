package com.tourly.booking.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.payment.entity.Payment;
import com.tourly.payment.repository.PaymentRepository;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.repository.TripRepository;

@Component
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TripRepository tripRepository;

    public BookingExpiryScheduler(
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            TripRepository tripRepository) {

        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.tripRepository = tripRepository;
    }

    // Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredBookings() {

        System.out.println("Checking expired bookings...");

        List<Booking> expiredBookings =
                bookingRepository.findExpiredBookings(LocalDateTime.now());

        for (Booking booking : expiredBookings) {

            // Safety: skip if already processed somehow
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }

            Trip trip = booking.getTrip();

            if (trip != null) {
                int updatedSeats = trip.getBookedSeats() - booking.getSeatsBooked();
                trip.setBookedSeats(Math.max(updatedSeats, 0));
                tripRepository.save(trip);
            }

            // Mark booking expired/cancelled
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(com.tourly.booking.enums.PaymentStatus.FAILED);
            booking.setUpdatedAt(LocalDateTime.now());

            bookingRepository.save(booking);

            // Also mark payment failed if still pending
            Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);

            if (payment != null && payment.getStatus() == com.tourly.payment.enums.PaymentStatus.PENDING) {
                payment.setStatus(com.tourly.payment.enums.PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        }
    }
}