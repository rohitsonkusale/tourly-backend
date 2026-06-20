package com.tourly.trip.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.tourly.trip.enums.CancellationPolicy;
import com.tourly.trip.enums.TripCategory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateTripRequest {

    // ── Core trip fields ──────────────────────────────────────

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    // ── Destination — find or create by city/state ────────────
    @NotBlank(message = "Destination city is required")
    private String destinationCity;   // e.g. "Panaji"

    private String destinationState;  // e.g. "Goa"

    // ── Dates ─────────────────────────────────────────────────

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    // ── Pricing ───────────────────────────────────────────────

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    private BigDecimal basePrice;

    // Dynamic pricing: admin enforces min/max from these percentages
    private BigDecimal maxDiscountPercent = BigDecimal.ZERO;  // e.g. 20 → min = base × 0.80
    private BigDecimal maxIncreasePercent = BigDecimal.ZERO;  // e.g. 40 → max = base × 1.40

    // ── Group & Logistics ─────────────────────────────────────

    @NotNull(message = "Total seats are required")
    @Positive(message = "Total seats must be greater than 0")
    private Integer totalSeats;

    private Integer minGroupSize;
    private Integer durationDays;
    private Integer durationNights;

    private String startsFrom;
    private String endsAt;
    private String tripType;
    private String difficulty;
    private String bestTime;

    @NotNull(message = "Trip category is required")
    private TripCategory category;

    @NotNull(message = "Cancellation policy is required")
    private CancellationPolicy cancellationPolicy;

    // JSON array of badge strings e.g. ["Adventure","Weekend Getaway"]
    private String badges;

    // ── About section ─────────────────────────────────────────

    private String aboutDescription;

    // [{icon, title}]
    private List<HighlightItem> highlights;

    // ── Gallery (Cloudinary URLs) ─────────────────────────────

    private List<String> galleryUrls;   // up to 5 Cloudinary URLs
    private String coverImageUrl;       // single cover photo URL

    // ── Itinerary ─────────────────────────────────────────────

    private List<ItineraryDayItem> itinerary;

    // ── Inclusions / Exclusions ───────────────────────────────

    private List<String> inclusions;
    private List<String> exclusions;

    // ── Stops ─────────────────────────────────────────────────

    private List<String> stops;

    // ── Stays ─────────────────────────────────────────────────

    private List<StayItem> stays;

    // ── Price Breakdown ───────────────────────────────────────

    private List<PriceBreakdownItem> priceBreakdown;

    private Boolean showPriceBifurcation;

    // ── Batches ───────────────────────────────────────────────

    private List<BatchItem> batches;

    // ═══════════════════════════════════════════════════════════
    // NESTED DTOs
    // ═══════════════════════════════════════════════════════════

    public static class HighlightItem {
        private String icon;
        private String title;
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    public static class ItineraryDayItem {
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

    public static class StayItem {
        private String name;
        private String location;
        private String description;
        private List<String> amenities;
        private List<String> images; // Cloudinary URLs
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<String> getAmenities() { return amenities; }
        public void setAmenities(List<String> amenities) { this.amenities = amenities; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
    }

    public static class PriceBreakdownItem {
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

    public static class BatchItem {
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

    // ═══════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }

    public String getDestinationState() { return destinationState; }
    public void setDestinationState(String destinationState) { this.destinationState = destinationState; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getMaxDiscountPercent() { return maxDiscountPercent; }
    public void setMaxDiscountPercent(BigDecimal maxDiscountPercent) { this.maxDiscountPercent = maxDiscountPercent; }

    public BigDecimal getMaxIncreasePercent() { return maxIncreasePercent; }
    public void setMaxIncreasePercent(BigDecimal maxIncreasePercent) { this.maxIncreasePercent = maxIncreasePercent; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

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

    public TripCategory getCategory() { return category; }
    public void setCategory(TripCategory category) { this.category = category; }

    public CancellationPolicy getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(CancellationPolicy cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }

    public String getBadges() { return badges; }
    public void setBadges(String badges) { this.badges = badges; }

    public String getAboutDescription() { return aboutDescription; }
    public void setAboutDescription(String aboutDescription) { this.aboutDescription = aboutDescription; }

    public List<HighlightItem> getHighlights() { return highlights; }
    public void setHighlights(List<HighlightItem> highlights) { this.highlights = highlights; }

    public List<String> getGalleryUrls() { return galleryUrls; }
    public void setGalleryUrls(List<String> galleryUrls) { this.galleryUrls = galleryUrls; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public List<ItineraryDayItem> getItinerary() { return itinerary; }
    public void setItinerary(List<ItineraryDayItem> itinerary) { this.itinerary = itinerary; }

    public List<String> getInclusions() { return inclusions; }
    public void setInclusions(List<String> inclusions) { this.inclusions = inclusions; }

    public List<String> getExclusions() { return exclusions; }
    public void setExclusions(List<String> exclusions) { this.exclusions = exclusions; }

    public List<String> getStops() { return stops; }
    public void setStops(List<String> stops) { this.stops = stops; }

    public List<StayItem> getStays() { return stays; }
    public void setStays(List<StayItem> stays) { this.stays = stays; }

    public List<PriceBreakdownItem> getPriceBreakdown() { return priceBreakdown; }
    public void setPriceBreakdown(List<PriceBreakdownItem> priceBreakdown) { this.priceBreakdown = priceBreakdown; }

    public Boolean getShowPriceBifurcation() { return showPriceBifurcation; }
    public void setShowPriceBifurcation(Boolean showPriceBifurcation) { this.showPriceBifurcation = showPriceBifurcation; }

    public List<BatchItem> getBatches() { return batches; }
    public void setBatches(List<BatchItem> batches) { this.batches = batches; }
}
