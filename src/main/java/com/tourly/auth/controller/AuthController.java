package com.tourly.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tourly.auth.dto.request.GoogleAuthRequest;
import com.tourly.auth.dto.request.LoginRequest;
import com.tourly.auth.dto.request.RegisterRequest;
import com.tourly.auth.dto.response.AuthResponse;
import com.tourly.auth.dto.response.UserResponse;
import com.tourly.auth.service.AuthService;
import com.tourly.common.dto.ApiResponse;
import com.tourly.auth.security.CurrentUser;
import com.tourly.auth.security.UserPrincipal;
import com.tourly.common.exception.UnauthorizedActionException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs for user registration and login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the selected role and returns registered user details"
    )
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response = authService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticates user credentials and returns JWT token with user authentication details"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.loginUser(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/google")
    @Operation(
            summary = "Authenticate with Google OAuth",
            description = "Verifies Google ID token and authenticates/registers user, returning JWT token"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request) {

        AuthResponse response = authService.googleAuth(request);

        return ResponseEntity.ok(ApiResponse.success("Google authentication successful", response));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current authenticated user",
            description = "Fetches the current authenticated user details using JWT token",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedActionException("User is not authenticated");
        }
        UserResponse response = authService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User profile fetched successfully", response));
    }
}