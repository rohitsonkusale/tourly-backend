package com.tourly.trip.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trip_stay_images")
public class TripStayImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_stay_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_stay_id", nullable = false)
    private TripStay stay;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public TripStayImage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TripStay getStay() { return stay; }
    public void setStay(TripStay stay) { this.stay = stay; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
