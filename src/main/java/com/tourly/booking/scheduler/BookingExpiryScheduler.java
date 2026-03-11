package com.tourly.booking.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.enums.PaymentStatus;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.trip.entity.Trip;

@Component
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;

    public BookingExpiryScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    public void releaseExpiredBookings() {

        System.out.println("Checking expired bookings...");

        List<Booking> expiredBookings =
                bookingRepository.findExpiredBookings(LocalDateTime.now());

        for (Booking booking : expiredBookings) {

            Trip trip = booking.getTrip();

            // release reserved seats
            trip.setBookedSeats(
                    trip.getBookedSeats() - booking.getSeatsBooked()
            );

            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.FAILED);

            bookingRepository.save(booking);
        }
    }
}