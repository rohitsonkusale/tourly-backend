package com.tourly.userprofile.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.tourly.auth.security.CurrentUser;
import com.tourly.auth.security.UserPrincipal;
import com.tourly.common.dto.ApiResponse;
import com.tourly.userprofile.dto.request.UserProfileRequest;
import com.tourly.userprofile.dto.response.UserProfileResponse;
import com.tourly.userprofile.service.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "User Profile", description = "APIs for managing user profile and public profile access")
public class UserProfileController {

    private final UserProfileService profileService;

    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    @Operation(
            summary = "Get current user profile",
            description = "Fetch authenticated user's profile details",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@CurrentUser UserPrincipal currentUser) {
        UserProfileResponse response = profileService.getProfile(currentUser.getId());

        ApiResponse<UserProfileResponse> apiResponse = new ApiResponse<>(
                true,
                "Profile fetched successfully",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping
    @Operation(
            summary = "Update current user profile",
            description = "Update authenticated user's profile information",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UserProfileRequest request) {

        UserProfileResponse response = profileService.updateProfile(currentUser.getId(), request);

        ApiResponse<UserProfileResponse> apiResponse = new ApiResponse<>(
                true,
                "Profile updated successfully",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/upload-picture")
    @Operation(
            summary = "Upload profile picture",
            description = "Upload and update authenticated user's profile picture",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfilePicture(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file) {

        String url = profileService.storeProfilePicture(currentUser.getId(), file);
        UserProfileResponse response = profileService.updateProfilePicture(currentUser.getId(), url);

        ApiResponse<UserProfileResponse> apiResponse = new ApiResponse<>(
                true,
                "Profile picture uploaded successfully",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/public/{userId}")
    @Operation(summary = "Get public user profile", description = "Fetch public profile details by user ID")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getPublicProfile(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long userId) {

        UserProfileResponse response = profileService.getPublicProfile(userId);

        ApiResponse<UserProfileResponse> apiResponse = new ApiResponse<>(
                true,
                "Public profile fetched successfully",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }
}