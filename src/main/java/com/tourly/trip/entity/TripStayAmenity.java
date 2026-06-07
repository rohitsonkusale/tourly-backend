package com.tourly.trip.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trip_stay_amenities")
public class TripStayAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_stay_amenity_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_stay_id", nullable = false)
    private TripStay stay;

    @Column(name = "amenity", nullable = false, length = 255)
    private String amenity;

    public TripStayAmenity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TripStay getStay() { return stay; }
    public void setStay(TripStay stay) { this.stay = stay; }
    public String getAmenity() { return amenity; }
    public void setAmenity(String amenity) { this.amenity = amenity; }
}
