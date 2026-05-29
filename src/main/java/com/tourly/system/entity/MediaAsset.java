package com.tourly.system.entity;

import com.tourly.auth.entity.User;
import com.tourly.system.enums.AssetMediaType;
import com.tourly.system.enums.MediaProvider;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_assets")
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private AssetMediaType fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MediaProvider provider;

    @Column(name = "provider_public_id", length = 255)
    private String providerPublicId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MediaAsset() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public AssetMediaType getFileType() { return fileType; }
    public void setFileType(AssetMediaType fileType) { this.fileType = fileType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public MediaProvider getProvider() { return provider; }
    public void setProvider(MediaProvider provider) { this.provider = provider; }
    public String getProviderPublicId() { return providerPublicId; }
    public void setProviderPublicId(String providerPublicId) { this.providerPublicId = providerPublicId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
