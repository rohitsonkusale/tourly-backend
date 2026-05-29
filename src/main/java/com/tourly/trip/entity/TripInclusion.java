package com.tourly.trip.entity;

import com.tourly.trip.enums.InclusionType;
import jakarta.persistence.*;

@Entity
@Table(name = "trip_inclusions")
public class TripInclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private TripBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InclusionType type;

    @Column(nullable = false)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public TripInclusion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TripBatch getBatch() {
        return batch;
    }

    public void setBatch(TripBatch batch) {
        this.batch = batch;
    }

    public InclusionType getType() {
        return type;
    }

    public void setType(InclusionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
