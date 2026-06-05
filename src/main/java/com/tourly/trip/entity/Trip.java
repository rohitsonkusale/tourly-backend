package com.tourly.trip.entity;

import com.tourly.auth.entity.User;
import com.tourly.trip.enums.*;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "planner_id")
    private User planner;

    @ManyToOne
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "min_price")
    private BigDecimal minPrice;

    @Column(name = "max_price")
    private BigDecimal maxPrice;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "booked_seats")
    private Integer bookedSeats;

    @Enumerated(EnumType.STRING)
    private TripCategory category;

    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy")
    private CancellationPolicy cancellationPolicy;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    private String difficulty;

    @Column(name = "group_size_label")
    private String groupSizeLabel;

    @Column(name = "trip_type")
    private String tripType;

    @Column(name = "best_time")
    private String bestTime;

    // ── New rich data fields ──────────────────────────────────
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

    @Column(name = "badges", columnDefinition = "TEXT")
    private String badges; // JSON array string e.g. ["Adventure","Weekend"]

    // ── Dynamic pricing ───────────────────────────────────────
    @Column(name = "max_discount_percent", precision = 5, scale = 2)
    private java.math.BigDecimal maxDiscountPercent = java.math.BigDecimal.ZERO;

    @Column(name = "max_increase_percent", precision = 5, scale = 2)
    private java.math.BigDecimal maxIncreasePercent = java.math.BigDecimal.ZERO;

    @Column(name = "current_price", precision = 12, scale = 2)
    private java.math.BigDecimal currentPrice;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "is_deleted")
    private Boolean deleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ── Child Collections ─────────────────────────────────────
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TripHighlight> highlights = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TripItineraryDay> itinerary = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TripStay> stays = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TripStop> stops = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TripItem> items = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TripMedia> media = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TripPriceBreakdown> priceBreakdown = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TripBatch> batches = new java.util.ArrayList<>();

    public Trip() {
    }

    public boolean hasAvailableSeats() {
        return bookedSeats != null
                && totalSeats != null
                && bookedSeats < totalSeats;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.active == null) {
            this.active = true;
        }

        if (this.deleted == null) {
            this.deleted = false;
        }

        if (this.bookedSeats == null) {
            this.bookedSeats = 0;
        }

        // Keep your existing business lifecycle default
        if (this.status == null) {
            this.status = TripStatus.DRAFT;
        }

        if (this.approvalStatus == null) {
            this.approvalStatus = ApprovalStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================
    // GETTERS & SETTERS
    // =========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getPlanner() {
        return planner;
    }

    public void setPlanner(User planner) {
        this.planner = planner;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Integer getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(Integer bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public TripCategory getCategory() {
        return category;
    }

    public void setCategory(TripCategory category) {
        this.category = category;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public CancellationPolicy getCancellationPolicy() {
        return cancellationPolicy;
    }

    public void setCancellationPolicy(CancellationPolicy cancellationPolicy) {
        this.cancellationPolicy = cancellationPolicy;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getGroupSizeLabel() {
        return groupSizeLabel;
    }

    public void setGroupSizeLabel(String groupSizeLabel) {
        this.groupSizeLabel = groupSizeLabel;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getBestTime() {
        return bestTime;
    }

    public void setBestTime(String bestTime) {
        this.bestTime = bestTime;
    }

    // ── New rich data fields getters/setters ──────────────────

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

    public String getBadges() { return badges; }
    public void setBadges(String badges) { this.badges = badges; }

    // ── Dynamic pricing getters/setters ───────────────────────

    public java.math.BigDecimal getMaxDiscountPercent() { return maxDiscountPercent; }
    public void setMaxDiscountPercent(java.math.BigDecimal maxDiscountPercent) { this.maxDiscountPercent = maxDiscountPercent; }

    public java.math.BigDecimal getMaxIncreasePercent() { return maxIncreasePercent; }
    public void setMaxIncreasePercent(java.math.BigDecimal maxIncreasePercent) { this.maxIncreasePercent = maxIncreasePercent; }

    public java.math.BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(java.math.BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    // ── Child Collections getters/setters ─────────────────────

    public java.util.List<TripHighlight> getHighlights() { return highlights; }
    public void setHighlights(java.util.List<TripHighlight> highlights) { this.highlights = highlights; }

    public java.util.List<TripItineraryDay> getItinerary() { return itinerary; }
    public void setItinerary(java.util.List<TripItineraryDay> itinerary) { this.itinerary = itinerary; }

    public java.util.List<TripStay> getStays() { return stays; }
    public void setStays(java.util.List<TripStay> stays) { this.stays = stays; }

    public java.util.List<TripStop> getStops() { return stops; }
    public void setStops(java.util.List<TripStop> stops) { this.stops = stops; }

    public java.util.List<TripItem> getItems() { return items; }
    public void setItems(java.util.List<TripItem> items) { this.items = items; }

    public java.util.List<TripMedia> getMedia() { return media; }
    public void setMedia(java.util.List<TripMedia> media) { this.media = media; }

    public java.util.List<TripPriceBreakdown> getPriceBreakdown() { return priceBreakdown; }
    public void setPriceBreakdown(java.util.List<TripPriceBreakdown> priceBreakdown) { this.priceBreakdown = priceBreakdown; }

    public java.util.List<TripBatch> getBatches() { return batches; }
    public void setBatches(java.util.List<TripBatch> batches) { this.batches = batches; }
}