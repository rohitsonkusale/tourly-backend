package com.tourly.auth.service;

import com.tourly.auth.dto.request.LoginRequest;
import com.tourly.auth.dto.request.RegisterRequest;
import com.tourly.auth.dto.response.AuthResponse;
import com.tourly.auth.dto.response.UserResponse;

public interface AuthService {

    UserResponse registerUser(RegisterRequest request);

    AuthResponse loginUser(LoginRequest request);

}