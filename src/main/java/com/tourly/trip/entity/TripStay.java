package com.tourly.trip.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trip_stays")
public class TripStay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "stay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripStayAmenity> amenities = new ArrayList<>();

    @OneToMany(mappedBy = "stay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripStayImage> images = new ArrayList<>();

    public TripStay() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public List<TripStayAmenity> getAmenities() { return amenities; }
    public void setAmenities(List<TripStayAmenity> amenities) { this.amenities = amenities; }

    public List<TripStayImage> getImages() { return images; }
    public void setImages(List<TripStayImage> images) { this.images = images; }
}
