package com.tourly.trip.dto.response;

import java.math.BigDecimal;

public class HostStatsResponse {

    private long upcomingTripsCount;
    private long totalBookingsCount;
    private BigDecimal monthlyEarnings;
    private long pendingActionsCount;

    public HostStatsResponse() {
    }

    public HostStatsResponse(long upcomingTripsCount, long totalBookingsCount, BigDecimal monthlyEarnings, long pendingActionsCount) {
        this.upcomingTripsCount = upcomingTripsCount;
        this.totalBookingsCount = totalBookingsCount;
        this.monthlyEarnings = monthlyEarnings;
        this.pendingActionsCount = pendingActionsCount;
    }

    public long getUpcomingTripsCount() {
        return upcomingTripsCount;
    }

    public void setUpcomingTripsCount(long upcomingTripsCount) {
        this.upcomingTripsCount = upcomingTripsCount;
    }

    public long getTotalBookingsCount() {
        return totalBookingsCount;
    }

    public void setTotalBookingsCount(long totalBookingsCount) {
        this.totalBookingsCount = totalBookingsCount;
    }

    public BigDecimal getMonthlyEarnings() {
        return monthlyEarnings;
    }

    public void setMonthlyEarnings(BigDecimal monthlyEarnings) {
        this.monthlyEarnings = monthlyEarnings;
    }

    public long getPendingActionsCount() {
        return pendingActionsCount;
    }

    public void setPendingActionsCount(long pendingActionsCount) {
        this.pendingActionsCount = pendingActionsCount;
    }
}
