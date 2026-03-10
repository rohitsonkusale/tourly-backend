package com.tourly.trip.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tourly.trip.enums.*;

public class CreateTripRequest {

    private String title;

    private String description;

    private Long destinationId;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal basePrice;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer totalSeats;

    private TripCategory category;

    private CancellationPolicy cancellationPolicy;

    // getters setters

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
     * @return Long return the destinationId
     */
    public Long getDestinationId() {
        return destinationId;
    }

    /**
     * @param destinationId the destinationId to set
     */
    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
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

}