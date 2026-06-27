package com.tourly.userprofile.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tourly.userprofile.dto.request.UserProfileRequest;
import com.tourly.userprofile.dto.response.UserProfileResponse;
import com.tourly.userprofile.entity.UserProfile;
import com.tourly.userprofile.mapper.UserProfileMapper;
import com.tourly.userprofile.repository.UserProfileRepository;
import com.tourly.userprofile.service.UserProfileService;
import com.tourly.auth.entity.RoleName;
import com.tourly.auth.repository.UserRepository;
import com.tourly.auth.entity.User;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.review.enums.TargetType;
import com.tourly.review.service.ReviewService;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepo;
    private final UserRepository userRepo;
    private final ReviewService reviewService;

    public UserProfileServiceImpl(UserProfileRepository profileRepo, UserRepository userRepo,
                                  ReviewService reviewService) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
        this.reviewService = reviewService;
    }

    // ==============================
    // Get full profile for current user
    // ==============================
    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfile profile = profileRepo.findByUser(user)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return profileRepo.save(newProfile);
                });

        return UserProfileMapper.toResponse(profile);
    }

    // ==============================
    // Update profile
    // ==============================
    @Override
    public UserProfileResponse updateProfile(Long userId, UserProfileRequest request) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfile profile = profileRepo.findByUser(user)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return profileRepo.save(newProfile);
                });

        UserProfileMapper.updateEntity(profile, request);
        return UserProfileMapper.toResponse(profileRepo.save(profile));
    }

    // ==============================
    // Upload profile picture (validates size and type)
    // ==============================
    @Override
    public String storeProfilePicture(Long userId, MultipartFile file) {
        // Validate file size (max 2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 2MB limit");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (!("image/jpeg".equals(contentType) || "image/png".equals(contentType))) {
            throw new BadRequestException("Only JPEG and PNG images are allowed");
        }

        String filename = "profile_" + userId + "_" + System.currentTimeMillis() + ".png";
        Path path = Paths.get("uploads/" + filename);

        try {
            Files.createDirectories(path.getParent());
            file.transferTo(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        return "/uploads/" + filename;
    }

    // ==============================
    // Update profile picture URL in DB
    // ==============================
    @Override
    public UserProfileResponse updateProfilePicture(Long userId, String url) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        profile.setProfilePictureUrl(url);
        return UserProfileMapper.toResponse(profileRepo.save(profile));
    }

    // ==============================
    // Public profile: hide sensitive info
    // ==============================
    @Override
    public UserProfileResponse getPublicProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        // Hide sensitive info for public
        profile.setContactEmail(null);
        profile.setContactPhone(null);

        UserProfileResponse response = UserProfileMapper.toResponse(profile);

        // Enrich with review data for hosts and planners
        RoleName roleName = user.getRole() != null ? user.getRole().getName() : null;
        if (roleName == RoleName.HOST) {
            response.setAverageRating(reviewService.getAverageRating(userId, TargetType.HOST));
            response.setReviewCount(reviewService.getReviewCount(userId, TargetType.HOST));
        } else if (roleName == RoleName.PLANNER) {
            response.setAverageRating(reviewService.getAverageRating(userId, TargetType.PLANNER));
            response.setReviewCount(reviewService.getReviewCount(userId, TargetType.PLANNER));
        }

        return response;
    }
}