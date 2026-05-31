package com.tourly.auth.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Public-facing host profile response.
 * Returned by GET /api/users/{id}/public-profile
 * Contains only information safe to expose publicly.
 */
public class PublicHostResponse {

    private Long id;
    private String fullName;
    private String avatar;
    private String instagramUsername;
    private String websiteUrl;
    private String roleName;
    private LocalDate createdDate;

    // Stats derived from trips and bookings
    private long totalTrips;
    private long publishedTrips;
    private long totalBookings;
    private BigDecimal totalRevenue;
    private double occupancyRate;

    // Verification flags
    private Boolean emailVerified;
    private Boolean kycVerified;
    private Boolean adminApproved;

    // Hosted trips (published only)
    private List<HostedTripSummary> hostedTrips;

    public static class HostedTripSummary {
        private Long id;
        private String title;
        private String destination;
        private String destinationState;
        private BigDecimal basePrice;
        private Integer totalSeats;
        private Integer bookedSeats;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private String category;
        private String status;

        public HostedTripSummary() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public String getDestinationState() { return destinationState; }
        public void setDestinationState(String destinationState) { this.destinationState = destinationState; }

        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

        public Integer getTotalSeats() { return totalSeats; }
        public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

        public Integer getBookedSeats() { return bookedSeats; }
        public void setBookedSeats(Integer bookedSeats) { this.bookedSeats = bookedSeats; }

        public java.time.LocalDate getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDate startDate) { this.startDate = startDate; }

        public java.time.LocalDate getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDate endDate) { this.endDate = endDate; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public PublicHostResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getInstagramUsername() { return instagramUsername; }
    public void setInstagramUsername(String instagramUsername) { this.instagramUsername = instagramUsername; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public long getTotalTrips() { return totalTrips; }
    public void setTotalTrips(long totalTrips) { this.totalTrips = totalTrips; }

    public long getPublishedTrips() { return publishedTrips; }
    public void setPublishedTrips(long publishedTrips) { this.publishedTrips = publishedTrips; }

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(double occupancyRate) { this.occupancyRate = occupancyRate; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getKycVerified() { return kycVerified; }
    public void setKycVerified(Boolean kycVerified) { this.kycVerified = kycVerified; }

    public Boolean getAdminApproved() { return adminApproved; }
    public void setAdminApproved(Boolean adminApproved) { this.adminApproved = adminApproved; }

    public List<HostedTripSummary> getHostedTrips() { return hostedTrips; }
    public void setHostedTrips(List<HostedTripSummary> hostedTrips) { this.hostedTrips = hostedTrips; }
}
