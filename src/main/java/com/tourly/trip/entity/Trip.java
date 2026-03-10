package com.tourly.trip.entity;

import com.tourly.auth.entity.User;
import com.tourly.trip.enums.*;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

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

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "is_deleted")
    private Boolean deleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean hasAvailableSeats() {
        return bookedSeats < totalSeats;
    }

    public Trip() {}

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = TripStatus.DRAFT;
        bookedSeats = 0;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters and setters

    /**
     * @return Long return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return String return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return String return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return User return the planner
     */
    public User getPlanner() {
        return planner;
    }

    /**
     * @param planner the planner to set
     */
    public void setPlanner(User planner) {
        this.planner = planner;
    }

    /**
     * @return Destination return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * @return LocalDate return the startDate
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * @return LocalDate return the endDate
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * @return BigDecimal return the basePrice
     */
    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * @param basePrice the basePrice to set
     */
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    /**
     * @return BigDecimal return the minPrice
     */
    public BigDecimal getMinPrice() {
        return minPrice;
    }

    /**
     * @param minPrice the minPrice to set
     */
    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    /**
     * @return BigDecimal return the maxPrice
     */
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    /**
     * @param maxPrice the maxPrice to set
     */
    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    /**
     * @return Integer return the totalSeats
     */
    public Integer getTotalSeats() {
        return totalSeats;
    }

    /**
     * @param totalSeats the totalSeats to set
     */
    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    /**
     * @return Integer return the bookedSeats
     */
    public Integer getBookedSeats() {
        return bookedSeats;
    }

    /**
     * @param bookedSeats the bookedSeats to set
     */
    public void setBookedSeats(Integer bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    /**
     * @return TripCategory return the category
     */
    public TripCategory getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(TripCategory category) {
        this.category = category;
    }

    /**
     * @return TripStatus return the status
     */
    public TripStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(TripStatus status) {
        this.status = status;
    }

    /**
     * @return CancellationPolicy return the cancellationPolicy
     */
    public CancellationPolicy getCancellationPolicy() {
        return cancellationPolicy;
    }

    /**
     * @param cancellationPolicy the cancellationPolicy to set
     */
    public void setCancellationPolicy(CancellationPolicy cancellationPolicy) {
        this.cancellationPolicy = cancellationPolicy;
    }

    /**
     * @return Boolean return the active
     */
    public Boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return Boolean return the deleted
     */
    public Boolean isDeleted() {
        return deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * @return LocalDateTime return the createdAt
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return LocalDateTime return the updatedAt
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt the updatedAt to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}