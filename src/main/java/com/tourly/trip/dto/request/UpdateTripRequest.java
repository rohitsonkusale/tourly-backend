package com.tourly.trip.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.tourly.trip.enums.CancellationPolicy;
import com.tourly.trip.enums.TripCategory;
import com.tourly.trip.enums.TripStatus;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpdateTripRequest {

    // ── Core fields (structural) ──────────────────────────────

    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    // Destination — by city/state (matches create flow)
    private String destinationCity;
    private String destinationState;
    private Long destinationId;

    // ── Dates (operational) ───────────────────────────────────

    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @FutureOrPresent(message = "End date cannot be in the past")
    private LocalDate endDate;

    // ── Pricing (operational) ─────────────────────────────────

    @Positive(message = "Base price must be greater than 0")
    private BigDecimal basePrice;

    @Positive(message = "Minimum price must be greater than 0")
    private BigDecimal minPrice;

    @Positive(message = "Maximum price must be greater than 0")
    private BigDecimal maxPrice;

    private BigDecimal maxDiscountPercent;
    private BigDecimal maxIncreasePercent;
    private Boolean showPriceBifurcation;

    // ── Seats (operational) ───────────────────────────────────

    @Positive(message = "Total seats must be greater than 0")
    private Integer totalSeats;

    // ── Logistics (structural) ────────────────────────────────

    private Integer minGroupSize;
    private Integer durationDays;
    private Integer durationNights;
    private String startsFrom;
    private String endsAt;
    private String tripType;
    private String difficulty;
    private String bestTime;

    private TripCategory category;
    private CancellationPolicy cancellationPolicy;
    private TripStatus status;
    private Boolean active;

    // JSON array of badge strings
    private String badges;

    // ── About section (structural) ────────────────────────────

    private String aboutDescription;

    // ── Gallery & Media (structural) ──────────────────────────

    private String coverImageUrl;
    private List<String> galleryUrls;

    // ── Child collections (structural except batches) ─────────

    private List<CreateTripRequest.HighlightItem> highlights;
    private List<CreateTripRequest.ItineraryDayItem> itinerary;
    private List<String> inclusions;
    private List<String> exclusions;
    private List<String> stops;
    private List<CreateTripRequest.StayItem> stays;
    private List<CreateTripRequest.PriceBreakdownItem> priceBreakdown;

    // ── Batches (operational) ─────────────────────────────────

    private List<CreateTripRequest.BatchItem> batches;

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

    public Long getDestinationId() { return destinationId; }
    public void setDestinationId(Long destinationId) { this.destinationId = destinationId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public BigDecimal getMaxDiscountPercent() { return maxDiscountPercent; }
    public void setMaxDiscountPercent(BigDecimal maxDiscountPercent) { this.maxDiscountPercent = maxDiscountPercent; }

    public BigDecimal getMaxIncreasePercent() { return maxIncreasePercent; }
    public void setMaxIncreasePercent(BigDecimal maxIncreasePercent) { this.maxIncreasePercent = maxIncreasePercent; }

    public Boolean getShowPriceBifurcation() { return showPriceBifurcation; }
    public void setShowPriceBifurcation(Boolean showPriceBifurcation) { this.showPriceBifurcation = showPriceBifurcation; }

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

    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getBadges() { return badges; }
    public void setBadges(String badges) { this.badges = badges; }

    public String getAboutDescription() { return aboutDescription; }
    public void setAboutDescription(String aboutDescription) { this.aboutDescription = aboutDescription; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public List<String> getGalleryUrls() { return galleryUrls; }
    public void setGalleryUrls(List<String> galleryUrls) { this.galleryUrls = galleryUrls; }

    public List<CreateTripRequest.HighlightItem> getHighlights() { return highlights; }
    public void setHighlights(List<CreateTripRequest.HighlightItem> highlights) { this.highlights = highlights; }

    public List<CreateTripRequest.ItineraryDayItem> getItinerary() { return itinerary; }
    public void setItinerary(List<CreateTripRequest.ItineraryDayItem> itinerary) { this.itinerary = itinerary; }

    public List<String> getInclusions() { return inclusions; }
    public void setInclusions(List<String> inclusions) { this.inclusions = inclusions; }

    public List<String> getExclusions() { return exclusions; }
    public void setExclusions(List<String> exclusions) { this.exclusions = exclusions; }

    public List<String> getStops() { return stops; }
    public void setStops(List<String> stops) { this.stops = stops; }

    public List<CreateTripRequest.StayItem> getStays() { return stays; }
    public void setStays(List<CreateTripRequest.StayItem> stays) { this.stays = stays; }

    public List<CreateTripRequest.PriceBreakdownItem> getPriceBreakdown() { return priceBreakdown; }
    public void setPriceBreakdown(List<CreateTripRequest.PriceBreakdownItem> priceBreakdown) { this.priceBreakdown = priceBreakdown; }

    public List<CreateTripRequest.BatchItem> getBatches() { return batches; }
    public void setBatches(List<CreateTripRequest.BatchItem> batches) { this.batches = batches; }
}
