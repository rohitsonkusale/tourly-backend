package com.tourly.userprofile.mapper;

import com.tourly.userprofile.dto.request.UserProfileRequest;
import com.tourly.userprofile.dto.response.UserProfileResponse;
import com.tourly.userprofile.entity.UserProfile;

public class UserProfileMapper {

    public static UserProfileResponse toResponse(UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setDisplayName(profile.getDisplayName());
        response.setBio(profile.getBio());
        response.setProfilePictureUrl(profile.getProfilePictureUrl());
        response.setContactEmail(profile.getContactEmail());
        response.setContactPhone(profile.getContactPhone());
        response.setSocialLinks(profile.getSocialLinks());
        response.setPreferredLanguage(profile.getPreferredLanguage());
        response.setTimezone(profile.getTimezone());
        return response;
    }

    public static void updateEntity(UserProfile profile, UserProfileRequest request) {
        if (request.getDisplayName() != null) profile.setDisplayName(request.getDisplayName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getProfilePictureUrl() != null) profile.setProfilePictureUrl(request.getProfilePictureUrl());
        if (request.getContactEmail() != null) profile.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) profile.setContactPhone(request.getContactPhone());
        if (request.getSocialLinks() != null) profile.setSocialLinks(request.getSocialLinks());
        if (request.getPreferredLanguage() != null) profile.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getTimezone() != null) profile.setTimezone(request.getTimezone());
    }
}