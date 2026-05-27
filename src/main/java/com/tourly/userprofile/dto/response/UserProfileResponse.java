package com.tourly.userprofile.dto.response;

import lombok.Data;

@Data
public class UserProfileResponse {

    private Long id;
    private Long userId;
    private String displayName;
    private String bio;
    private String profilePictureUrl;
    private String contactEmail;
    private String contactPhone;
    private String socialLinks;
    private String preferredLanguage;
    private String timezone;
}