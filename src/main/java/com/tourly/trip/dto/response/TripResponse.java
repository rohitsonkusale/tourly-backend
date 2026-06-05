package com.tourly.trip.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.tourly.trip.enums.ApprovalStatus;
import com.tourly.trip.enums.TripCategory;
import com.tourly.trip.enums.TripStatus;

public class TripResponse {

    private Long id;
    private String title;
    private String description;

    // Destination fields
    private String destination;       // city name
    private String destinationState;  // state name

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal basePrice;
    private Integer totalSeats;
    private Integer bookedSeats;

    // Rich Data Fields
    private Integer minGroupSize;
    private Integer durationDays;
    private Integer durationNights;
    private String startsFrom;
    private String endsAt;
    private String tripType;
    private String difficulty;
    private String bestTime;
    private String badges;
    private String aboutDescription;
    private String coverImageUrl;

    private java.util.List<HighlightResponse> highlights;
    private java.util.List<String> galleryUrls;
    private java.util.List<ItineraryDayResponse> itinerary;
    private java.util.List<String> inclusions;
    private java.util.List<String> exclusions;
    private java.util.List<String> stops;
    private java.util.List<StayResponse> stays;
    private java.util.List<PriceBreakdownResponse> priceBreakdown;
    private java.util.List<BatchResponse> batches;

    // People
    private String plannerName;
    private String hostName;

    // Trip metadata
    private TripCategory category;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;

    // Admin / owner fields
    private Boolean active;
    private Boolean deleted;
    private TripStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // =========================
    // Getters & Setters
    // =========================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDestinationState() { return destinationState; }
    public void setDestinationState(String destinationState) { this.destinationState = destinationState; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public Integer getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(Integer bookedSeats) { this.bookedSeats = bookedSeats; }

    public String getPlannerName() { return plannerName; }
    public void setPlannerName(String plannerName) { this.plannerName = plannerName; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public TripCategory getCategory() { return category; }
    public void setCategory(TripCategory category) { this.category = category; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public Boolean isActive() { return active; }
    public Boolean isDeleted() { return deleted; }

    // =========================
    // Rich Data Getters/Setters
    // =========================

    public Integer getMinGroupSize() { return minGroupSize; }
    public void setMinGroupSize(Integer minGroupSize) { this.minGroupSize = minGroupSize; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public Integer getDurationNights() { return durationNights; }
    public void setDurationNights(Integer durationNights) { this.durationNights = durationNights; }

    public String getStartsFrom() { return startsFrom; }
    public void setStartsFrom(String startsFrom) { this.startsFrom = startsFrom; }

    public String getEndsAt() { return endsAt; }
    public void setEndsAt(String endsAt) { this.endsAt = endsAt; }

    public String getTripType() { return tripType; }
    public void setTripType(String tripType) { this.tripType = tripType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getBestTime() { return bestTime; }
    public void setBestTime(String bestTime) { this.bestTime = bestTime; }

    public String getBadges() { return badges; }
    public void setBadges(String badges) { this.badges = badges; }

    public String getAboutDescription() { return aboutDescription; }
    public void setAboutDescription(String aboutDescription) { this.aboutDescription = aboutDescription; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public java.util.List<HighlightResponse> getHighlights() { return highlights; }
    public void setHighlights(java.util.List<HighlightResponse> highlights) { this.highlights = highlights; }

    public java.util.List<String> getGalleryUrls() { return galleryUrls; }
    public void setGalleryUrls(java.util.List<String> galleryUrls) { this.galleryUrls = galleryUrls; }

    public java.util.List<ItineraryDayResponse> getItinerary() { return itinerary; }
    public void setItinerary(java.util.List<ItineraryDayResponse> itinerary) { this.itinerary = itinerary; }

    public java.util.List<String> getInclusions() { return inclusions; }
    public void setInclusions(java.util.List<String> inclusions) { this.inclusions = inclusions; }

    public java.util.List<String> getExclusions() { return exclusions; }
    public void setExclusions(java.util.List<String> exclusions) { this.exclusions = exclusions; }

    public java.util.List<String> getStops() { return stops; }
    public void setStops(java.util.List<String> stops) { this.stops = stops; }

    public java.util.List<StayResponse> getStays() { return stays; }
    public void setStays(java.util.List<StayResponse> stays) { this.stays = stays; }

    public java.util.List<PriceBreakdownResponse> getPriceBreakdown() { return priceBreakdown; }
    public void setPriceBreakdown(java.util.List<PriceBreakdownResponse> priceBreakdown) { this.priceBreakdown = priceBreakdown; }

    public java.util.List<BatchResponse> getBatches() { return batches; }
    public void setBatches(java.util.List<BatchResponse> batches) { this.batches = batches; }

    // =========================
    // Nested DTOs
    // =========================

    public static class HighlightResponse {
        private String icon;
        private String title;
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    public static class ItineraryDayResponse {
        private Integer day;
        private String title;
        private String description;
        private String stay;
        private String meals;
        public Integer getDay() { return day; }
        public void setDay(Integer day) { this.day = day; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStay() { return stay; }
        public void setStay(String stay) { this.stay = stay; }
        public String getMeals() { return meals; }
        public void setMeals(String meals) { this.meals = meals; }
    }

    public static class StayResponse {
        private String name;
        private String location;
        private String description;
        private java.util.List<String> amenities;
        private java.util.List<String> images;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public java.util.List<String> getAmenities() { return amenities; }
        public void setAmenities(java.util.List<String> amenities) { this.amenities = amenities; }
        public java.util.List<String> getImages() { return images; }
        public void setImages(java.util.List<String> images) { this.images = images; }
    }

    public static class PriceBreakdownResponse {
        private String category;
        private BigDecimal amount;
        private String description;
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class BatchResponse {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal price;
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}