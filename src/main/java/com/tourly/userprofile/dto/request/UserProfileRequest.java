package com.tourly.userprofile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileRequest {

    @Size(max = 150)
    private String displayName;

    @Size(max = 500)
    private String bio;

    @Size(max = 255)
    private String profilePictureUrl;

    @Email
    private String contactEmail;

    @Size(max = 50)
    private String contactPhone;

    @Size(max = 255)
    private String socialLinks;

    @Size(max = 100)
    private String preferredLanguage;

    @Size(max = 100)
    private String timezone;
}