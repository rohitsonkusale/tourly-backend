package com.tourly.admin.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AdminDashboardResponse {

    // ===========================
    // Revenue Stats
    // ===========================
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal platformCommission;
    private BigDecimal avgTripValue;

    // ===========================
    // User Counts
    // ===========================
    private long activeHosts;
    private long activePlanners;
    private long activeTravellers;

    // ===========================
    // Booking / Trip Stats
    // ===========================
    private long totalTrips;
    private long totalBookings;
    private long pendingApprovals;
    private long refundRequests;
    private long openTickets;

    // ===========================
    // Conversion Metrics
    // ===========================
    private double bookingConversion;
    private double repeatBookingRate;

    // ===========================
    // Chart Data
    // ===========================
    private List<DailyRevenue> dailyRevenue;
    private List<DestinationEarning> destinationEarnings;
    private List<TopDestination> topDestinations;
    private List<TopHost> topHosts;

    // ===========================
    // Inner DTOs
    // ===========================

    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal amount;

        public DailyRevenue() {}

        public DailyRevenue(LocalDate date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class DestinationEarning {
        private String state;
        private BigDecimal earnings;

        public DestinationEarning() {}

        public DestinationEarning(String state, BigDecimal earnings) {
            this.state = state;
            this.earnings = earnings;
        }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public BigDecimal getEarnings() { return earnings; }
        public void setEarnings(BigDecimal earnings) { this.earnings = earnings; }
    }

    public static class TopDestination {
        private String city;
        private long bookingCount;

        public TopDestination() {}

        public TopDestination(String city, long bookingCount) {
            this.city = city;
            this.bookingCount = bookingCount;
        }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public long getBookingCount() { return bookingCount; }
        public void setBookingCount(long bookingCount) { this.bookingCount = bookingCount; }
    }

    public static class TopHost {
        private String hostName;
        private BigDecimal totalEarnings;

        public TopHost() {}

        public TopHost(String hostName, BigDecimal totalEarnings) {
            this.hostName = hostName;
            this.totalEarnings = totalEarnings;
        }

        public String getHostName() { return hostName; }
        public void setHostName(String hostName) { this.hostName = hostName; }
        public BigDecimal getTotalEarnings() { return totalEarnings; }
        public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    }

    // ===========================
    // Getters & Setters
    // ===========================

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }

    public BigDecimal getPlatformCommission() { return platformCommission; }
    public void setPlatformCommission(BigDecimal platformCommission) { this.platformCommission = platformCommission; }

    public BigDecimal getAvgTripValue() { return avgTripValue; }
    public void setAvgTripValue(BigDecimal avgTripValue) { this.avgTripValue = avgTripValue; }

    public long getActiveHosts() { return activeHosts; }
    public void setActiveHosts(long activeHosts) { this.activeHosts = activeHosts; }

    public long getActivePlanners() { return activePlanners; }
    public void setActivePlanners(long activePlanners) { this.activePlanners = activePlanners; }

    public long getActiveTravellers() { return activeTravellers; }
    public void setActiveTravellers(long activeTravellers) { this.activeTravellers = activeTravellers; }

    public long getTotalTrips() { return totalTrips; }
    public void setTotalTrips(long totalTrips) { this.totalTrips = totalTrips; }

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

    public long getPendingApprovals() { return pendingApprovals; }
    public void setPendingApprovals(long pendingApprovals) { this.pendingApprovals = pendingApprovals; }

    public long getRefundRequests() { return refundRequests; }
    public void setRefundRequests(long refundRequests) { this.refundRequests = refundRequests; }

    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }

    public double getBookingConversion() { return bookingConversion; }
    public void setBookingConversion(double bookingConversion) { this.bookingConversion = bookingConversion; }

    public double getRepeatBookingRate() { return repeatBookingRate; }
    public void setRepeatBookingRate(double repeatBookingRate) { this.repeatBookingRate = repeatBookingRate; }

    public List<DailyRevenue> getDailyRevenue() { return dailyRevenue; }
    public void setDailyRevenue(List<DailyRevenue> dailyRevenue) { this.dailyRevenue = dailyRevenue; }

    public List<DestinationEarning> getDestinationEarnings() { return destinationEarnings; }
    public void setDestinationEarnings(List<DestinationEarning> destinationEarnings) { this.destinationEarnings = destinationEarnings; }

    public List<TopDestination> getTopDestinations() { return topDestinations; }
    public void setTopDestinations(List<TopDestination> topDestinations) { this.topDestinations = topDestinations; }

    public List<TopHost> getTopHosts() { return topHosts; }
    public void setTopHosts(List<TopHost> topHosts) { this.topHosts = topHosts; }
}
