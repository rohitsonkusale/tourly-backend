package com.tourly.booking.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.repository.BookingRepository;

@Component
public class TripCompletionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TripCompletionScheduler.class);

    private final BookingRepository bookingRepository;

    public TripCompletionScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Runs once per day at 1:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void completeTrips() {
        try {
            List<Booking> bookings = bookingRepository.findCompletedTrips(LocalDate.now());

            if (bookings.isEmpty()) {
                logger.info("TripCompletionScheduler executed: no bookings to complete");
                return;
            }

            for (Booking booking : bookings) {
                booking.setStatus(BookingStatus.COMPLETED);
            }

            bookingRepository.saveAll(bookings);

            logger.info("TripCompletionScheduler executed: {} bookings marked as COMPLETED", bookings.size());

        } catch (Exception e) {
            logger.error("TripCompletionScheduler failed: {}", e.getMessage(), e);
        }
    }
}