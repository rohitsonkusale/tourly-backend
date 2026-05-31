package com.tourly.trip.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class HostAnalyticsResponse {

    private long totalTrips;
    private long publishedTrips;
    private long draftTrips;
    private long totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal avgRevenuePerTrip;
    private long totalSeatsOffered;
    private long totalSeatsBooked;
    private double occupancyRate; // percentage

    // Top trips by bookings
    private List<TripBookingSummary> topTrips;

    public static class TripBookingSummary {
        private Long tripId;
        private String tripTitle;
        private String destination;
        private long bookingCount;
        private BigDecimal revenue;

        public TripBookingSummary() {}

        public TripBookingSummary(Long tripId, String tripTitle, String destination,
                                  long bookingCount, BigDecimal revenue) {
            this.tripId = tripId;
            this.tripTitle = tripTitle;
            this.destination = destination;
            this.bookingCount = bookingCount;
            this.revenue = revenue;
        }

        public Long getTripId() { return tripId; }
        public void setTripId(Long tripId) { this.tripId = tripId; }

        public String getTripTitle() { return tripTitle; }
        public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public long getBookingCount() { return bookingCount; }
        public void setBookingCount(long bookingCount) { this.bookingCount = bookingCount; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }

    public HostAnalyticsResponse() {}

    public long getTotalTrips() { return totalTrips; }
    public void setTotalTrips(long totalTrips) { this.totalTrips = totalTrips; }

    public long getPublishedTrips() { return publishedTrips; }
    public void setPublishedTrips(long publishedTrips) { this.publishedTrips = publishedTrips; }

    public long getDraftTrips() { return draftTrips; }
    public void setDraftTrips(long draftTrips) { this.draftTrips = draftTrips; }

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getAvgRevenuePerTrip() { return avgRevenuePerTrip; }
    public void setAvgRevenuePerTrip(BigDecimal avgRevenuePerTrip) { this.avgRevenuePerTrip = avgRevenuePerTrip; }

    public long getTotalSeatsOffered() { return totalSeatsOffered; }
    public void setTotalSeatsOffered(long totalSeatsOffered) { this.totalSeatsOffered = totalSeatsOffered; }

    public long getTotalSeatsBooked() { return totalSeatsBooked; }
    public void setTotalSeatsBooked(long totalSeatsBooked) { this.totalSeatsBooked = totalSeatsBooked; }

    public double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(double occupancyRate) { this.occupancyRate = occupancyRate; }

    public List<TripBookingSummary> getTopTrips() { return topTrips; }
    public void setTopTrips(List<TripBookingSummary> topTrips) { this.topTrips = topTrips; }
}
