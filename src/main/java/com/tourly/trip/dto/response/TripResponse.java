package com.tourly.trip.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TripResponse {

    private Long id;

    private String title;

    private String description;

    private String destination;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal basePrice;

    private Integer totalSeats;

    private Integer bookedSeats;

    private String plannerName;

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
     * @return String return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
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
     * @return String return the plannerName
     */
    public String getPlannerName() {
        return plannerName;
    }

    /**
     * @param plannerName the plannerName to set
     */
    public void setPlannerName(String plannerName) {
        this.plannerName = plannerName;
    }

}