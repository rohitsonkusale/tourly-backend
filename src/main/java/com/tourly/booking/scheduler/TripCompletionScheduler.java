package com.tourly.booking.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.repository.BookingRepository;

@Component
public class TripCompletionScheduler {

    private final BookingRepository bookingRepository;

    public TripCompletionScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // runs once per day
    @Scheduled(cron = "0 0 1 * * ?")
    public void completeTrips() {

        List<Booking> bookings =
                bookingRepository.findCompletedTrips(LocalDate.now());

        for (Booking booking : bookings) {

            booking.setStatus(BookingStatus.COMPLETED);

            bookingRepository.save(booking);
        }

        System.out.println("Trip completion scheduler executed");
    }
}