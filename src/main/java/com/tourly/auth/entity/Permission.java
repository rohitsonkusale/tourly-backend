package com.tourly.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_name", nullable = false, unique = true, length = 100)
    private PermissionName permissionName;

    @Column(name = "description", length = 255)
    private String description;

    public Permission() {}

    public Permission(Long id, PermissionName permissionName, String description) {
        this.id = id;
        this.permissionName = permissionName;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PermissionName getPermissionName() { return permissionName; }
    public void setPermissionName(PermissionName permissionName) { this.permissionName = permissionName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
