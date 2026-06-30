package com.tourly.admin.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tourly.admin.dto.response.AdminDashboardResponse;
import com.tourly.admin.dto.response.AdminDashboardResponse.DailyRevenue;
import com.tourly.admin.dto.response.AdminDashboardResponse.DestinationEarning;
import com.tourly.admin.dto.response.AdminDashboardResponse.TopDestination;
import com.tourly.admin.dto.response.AdminDashboardResponse.TopHost;
import com.tourly.admin.service.AdminDashboardService;
import com.tourly.auth.entity.AccountStatus;
import com.tourly.auth.entity.RoleName;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.support.enums.TicketStatus;
import com.tourly.support.repository.SupportTicketRepository;
import com.tourly.payment.enums.RefundStatus;
import com.tourly.payment.repository.RefundRepository;
import com.tourly.trip.repository.TripRepository;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final RefundRepository refundRepository;

    public AdminDashboardServiceImpl(BookingRepository bookingRepository,
                                     UserRepository userRepository,
                                     TripRepository tripRepository,
                                     SupportTicketRepository supportTicketRepository,
                                     RefundRepository refundRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.refundRepository = refundRepository;
    }

    @Override
    public AdminDashboardResponse getDashboardStats() {
        AdminDashboardResponse response = new AdminDashboardResponse();

        // ===========================
        // 1. Revenue Stats
        // ===========================
        BigDecimal totalRevenue = bookingRepository.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        response.setTotalRevenue(totalRevenue);

        YearMonth currentMonth = YearMonth.now();
        BigDecimal monthlyRevenue = bookingRepository.sumRevenueForMonth(
                currentMonth.getYear(), currentMonth.getMonthValue());
        if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;
        response.setMonthlyRevenue(monthlyRevenue);

        // Platform commission = 10% of total revenue
        response.setPlatformCommission(
                totalRevenue.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP));

        // Avg trip value = total revenue / confirmed bookings
        List<BookingStatus> confirmedStatuses = Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);
        long confirmedBookings = bookingRepository.countByStatusIn(confirmedStatuses);
        if (confirmedBookings > 0) {
            response.setAvgTripValue(
                    totalRevenue.divide(BigDecimal.valueOf(confirmedBookings), 2, RoundingMode.HALF_UP));
        } else {
            response.setAvgTripValue(BigDecimal.ZERO);
        }

        // ===========================
        // 2. User Counts
        // ===========================
        response.setActiveHosts(
                userRepository.countByRole_NameAndAccountStatusAndDeletedAtIsNull(RoleName.HOST, AccountStatus.ACTIVE));
        response.setActivePlanners(
                userRepository.countByRole_NameAndAccountStatusAndDeletedAtIsNull(RoleName.PLANNER, AccountStatus.ACTIVE));
        response.setActiveTravellers(
                userRepository.countByRole_NameAndAccountStatusAndDeletedAtIsNull(RoleName.TRAVELER, AccountStatus.ACTIVE));

        // ===========================
        // 3. Booking / Trip Stats
        // ===========================
        response.setTotalTrips(tripRepository.countByDeletedFalse());
        // Exclude cancelled bookings from total count
        List<BookingStatus> nonCancelledStatuses = Arrays.asList(
                BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.COMPLETED, BookingStatus.EXPIRED);
        response.setTotalBookings(bookingRepository.countByStatusIn(nonCancelledStatuses));
        response.setPendingApprovals(
                userRepository.countByAccountStatus(AccountStatus.PENDING_VERIFICATION));

        // Refund requests = pending refunds from the refunds table
        response.setRefundRequests(
                refundRepository.countByStatus(RefundStatus.PENDING));

        // Open tickets = OPEN + IN_PROGRESS
        response.setOpenTickets(
                supportTicketRepository.countByStatusIn(
                        Arrays.asList(TicketStatus.OPEN, TicketStatus.IN_PROGRESS)));

        // ===========================
        // 4. Conversion Metrics
        // ===========================
        long totalBookings = bookingRepository.count();
        if (totalBookings > 0) {
            response.setBookingConversion(
                    Math.round(((double) confirmedBookings / totalBookings) * 1000.0) / 10.0);
        } else {
            response.setBookingConversion(0.0);
        }

        // Repeat booking rate
        long distinctTravelers = bookingRepository.countDistinctTravelers();
        if (distinctTravelers > 0) {
            long repeatTravelers = bookingRepository.countRepeatTravelers();
            response.setRepeatBookingRate(
                    Math.round(((double) repeatTravelers / distinctTravelers) * 1000.0) / 10.0);
        } else {
            response.setRepeatBookingRate(0.0);
        }

        // ===========================
        // 5. 7-Day Revenue Chart
        // ===========================
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> dailyRows = bookingRepository.sumDailyRevenue(sevenDaysAgo);
        List<DailyRevenue> dailyRevenueList = new ArrayList<>();
        for (Object[] row : dailyRows) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            dailyRevenueList.add(new DailyRevenue(date, amount != null ? amount : BigDecimal.ZERO));
        }
        response.setDailyRevenue(dailyRevenueList);

        // ===========================
        // 6. Destination Earnings
        // ===========================
        List<Object[]> destRows = bookingRepository.sumRevenueByDestinationState();
        List<DestinationEarning> destinationEarnings = destRows.stream()
                .map(row -> new DestinationEarning(
                        (String) row[0],
                        row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO))
                .collect(Collectors.toList());
        response.setDestinationEarnings(destinationEarnings);

        // ===========================
        // 7. Top 5 Destinations by Bookings
        // ===========================
        List<Object[]> topDestRows = bookingRepository.countBookingsByDestination();
        List<TopDestination> topDestinations = topDestRows.stream()
                .limit(5)
                .map(row -> new TopDestination(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
        response.setTopDestinations(topDestinations);

        // ===========================
        // 8. Top 5 Hosts by Revenue
        // ===========================
        List<Object[]> topHostRows = bookingRepository.sumRevenueByHost();
        List<TopHost> topHosts = topHostRows.stream()
                .limit(5)
                .map(row -> new TopHost(
                        (String) row[0],
                        row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO))
                .collect(Collectors.toList());
        response.setTopHosts(topHosts);

        return response;
    }
}

