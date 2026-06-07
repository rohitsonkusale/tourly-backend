package com.tourly.trip.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trip_badges", uniqueConstraints = {
    @UniqueConstraint(name = "uq_trip_badges_trip_badge", columnNames = {"trip_id", "badge_name"})
})
public class TripBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_badge_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    public TripBadge() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }
    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }
}
