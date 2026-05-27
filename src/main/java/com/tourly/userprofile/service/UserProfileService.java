package com.tourly.userprofile.service;

import org.springframework.web.multipart.MultipartFile;

import com.tourly.userprofile.dto.request.UserProfileRequest;
import com.tourly.userprofile.dto.response.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UserProfileRequest request);

    String storeProfilePicture(Long userId, MultipartFile file);
    UserProfileResponse updateProfilePicture(Long userId, String url);

    UserProfileResponse getPublicProfile(Long userId);
}