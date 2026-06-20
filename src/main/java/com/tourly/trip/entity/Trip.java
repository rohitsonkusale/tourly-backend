package com.tourly.trip.entity;

import com.tourly.auth.entity.User;
import com.tourly.trip.enums.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id")
    private User planner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private User host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "min_price", precision = 12, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "current_price", precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "max_discount_percent", precision = 5, scale = 2)
    private BigDecimal maxDiscountPercent = BigDecimal.ZERO;

    @Column(name = "max_increase_percent", precision = 5, scale = 2)
    private BigDecimal maxIncreasePercent = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "booked_seats")
    private Integer bookedSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private TripCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TripStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", length = 50)
    private CancellationPolicy cancellationPolicy;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "difficulty", length = 50)
    private String difficulty;

    @Column(name = "group_size_label", length = 50)
    private String groupSizeLabel;

    @Column(name = "trip_type", length = 100)
    private String tripType;

    @Column(name = "best_time", length = 100)
    private String bestTime;

    @Column(name = "starts_from", length = 255)
    private String startsFrom;

    @Column(name = "ends_at", length = 255)
    private String endsAt;

    @Column(name = "about_description", columnDefinition = "TEXT")
    private String aboutDescription;

    @Column(name = "min_group_size")
    private Integer minGroupSize;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "duration_nights")
    private Integer durationNights;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "show_price_bifurcation", nullable = false)
    private Boolean showPriceBifurcation = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Child collections
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripHighlight> highlights = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripItineraryDay> itinerary = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripStay> stays = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripStop> stops = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripPriceBreakdown> priceBreakdown = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripBatch> batches = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripBadge> badges = new ArrayList<>();

    public Trip() {}

    public boolean hasAvailableSeats() {
        return bookedSeats != null && totalSeats != null && bookedSeats < totalSeats;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) this.active = true;
        if (this.deleted == null) this.deleted = false;
        if (this.bookedSeats == null) this.bookedSeats = 0;
        if (this.status == null) this.status = TripStatus.DRAFT;
        if (this.approvalStatus == null) this.approvalStatus = ApprovalStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getPlanner() { return planner; }
    public void setPlanner(User planner) { this.planner = planner; }
    public User getHost() { return host; }
    public void setHost(User host) { this.host = host; }
    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getMaxDiscountPercent() { return maxDiscountPercent; }
    public void setMaxDiscountPercent(BigDecimal maxDiscountPercent) { this.maxDiscountPercent = maxDiscountPercent; }
    public BigDecimal getMaxIncreasePercent() { return maxIncreasePercent; }
    public void setMaxIncreasePercent(BigDecimal maxIncreasePercent) { this.maxIncreasePercent = maxIncreasePercent; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    public Integer getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(Integer bookedSeats) { this.bookedSeats = bookedSeats; }
    public TripCategory getCategory() { return category; }
    public void setCategory(TripCategory category) { this.category = category; }
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public CancellationPolicy getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(CancellationPolicy cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getGroupSizeLabel() { return groupSizeLabel; }
    public void setGroupSizeLabel(String groupSizeLabel) { this.groupSizeLabel = groupSizeLabel; }
    public String getTripType() { return tripType; }
    public void setTripType(String tripType) { this.tripType = tripType; }
    public String getBestTime() { return bestTime; }
    public void setBestTime(String bestTime) { this.bestTime = bestTime; }
    public String getStartsFrom() { return startsFrom; }
    public void setStartsFrom(String startsFrom) { this.startsFrom = startsFrom; }
    public String getEndsAt() { return endsAt; }
    public void setEndsAt(String endsAt) { this.endsAt = endsAt; }
    public String getAboutDescription() { return aboutDescription; }
    public void setAboutDescription(String aboutDescription) { this.aboutDescription = aboutDescription; }
    public Integer getMinGroupSize() { return minGroupSize; }
    public void setMinGroupSize(Integer minGroupSize) { this.minGroupSize = minGroupSize; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Integer getDurationNights() { return durationNights; }
    public void setDurationNights(Integer durationNights) { this.durationNights = durationNights; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public Boolean getShowPriceBifurcation() { return showPriceBifurcation; }
    public void setShowPriceBifurcation(Boolean showPriceBifurcation) { this.showPriceBifurcation = showPriceBifurcation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public List<TripHighlight> getHighlights() { return highlights; }
    public void setHighlights(List<TripHighlight> highlights) { this.highlights = highlights; }
    public List<TripItineraryDay> getItinerary() { return itinerary; }
    public void setItinerary(List<TripItineraryDay> itinerary) { this.itinerary = itinerary; }
    public List<TripStay> getStays() { return stays; }
    public void setStays(List<TripStay> stays) { this.stays = stays; }
    public List<TripStop> getStops() { return stops; }
    public void setStops(List<TripStop> stops) { this.stops = stops; }
    public List<TripItem> getItems() { return items; }
    public void setItems(List<TripItem> items) { this.items = items; }
    public List<TripMedia> getMedia() { return media; }
    public void setMedia(List<TripMedia> media) { this.media = media; }
    public List<TripPriceBreakdown> getPriceBreakdown() { return priceBreakdown; }
    public void setPriceBreakdown(List<TripPriceBreakdown> priceBreakdown) { this.priceBreakdown = priceBreakdown; }
    public List<TripBatch> getBatches() { return batches; }
    public void setBatches(List<TripBatch> batches) { this.batches = batches; }
    public List<TripBadge> getBadges() { return badges; }
    public void setBadges(List<TripBadge> badges) { this.badges = badges; }
}
