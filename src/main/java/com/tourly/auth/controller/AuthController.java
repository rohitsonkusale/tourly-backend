package com.tourly.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tourly.auth.dto.request.ForgotPasswordRequest;
import com.tourly.auth.dto.request.GoogleAuthRequest;
import com.tourly.auth.dto.request.LoginRequest;
import com.tourly.auth.dto.request.RegisterRequest;
import com.tourly.auth.dto.request.ResetPasswordRequest;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs for user registration and login")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.expiration:3600000}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiry;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

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
            description = "Authenticates user credentials, sets httpOnly cookies, and returns user details"
    )
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.loginUser(request);

        HttpHeaders headers = buildAuthCookieHeaders(response.getToken(), response.getRefreshToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("Login successful", response.getUser()));
    }

    @PostMapping("/google")
    @Operation(
            summary = "Authenticate with Google OAuth",
            description = "Verifies Google ID token, sets httpOnly cookies, and returns user details"
    )
    public ResponseEntity<ApiResponse<UserResponse>> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request) {

        AuthResponse response = authService.googleAuth(request);

        HttpHeaders headers = buildAuthCookieHeaders(response.getToken(), response.getRefreshToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("Google authentication successful", response.getUser()));
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

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh JWT token",
            description = "Reads refresh token from httpOnly cookie (or request body as fallback), returns new tokens"
    )
    public ResponseEntity<ApiResponse<UserResponse>> refreshToken(
            HttpServletRequest httpRequest,
            @RequestBody(required = false) java.util.Map<String, String> requestBody) {

        // 1. Try to read refresh token from httpOnly cookie
        String refreshToken = extractCookieValue(httpRequest, "refresh_token");

        // 2. Fallback: read from request body (backward compatibility)
        if ((refreshToken == null || refreshToken.isBlank()) && requestBody != null) {
            refreshToken = requestBody.get("refreshToken");
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new com.tourly.common.exception.BadRequestException("Refresh token is required");
        }

        AuthResponse response = authService.refreshToken(refreshToken);

        HttpHeaders headers = buildAuthCookieHeaders(response.getToken(), response.getRefreshToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("Token refreshed successfully", response.getUser()));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Clears authentication cookies"
    )
    public ResponseEntity<ApiResponse<Void>> logout() {
        HttpHeaders headers = new HttpHeaders();

        // Clear access token cookie
        ResponseCookie clearAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        // Clear refresh token cookie
        ResponseCookie clearRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        headers.add(HttpHeaders.SET_COOKIE, clearAccess.toString());
        headers.add(HttpHeaders.SET_COOKIE, clearRefresh.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset link to the user's registered email address"
    )
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("If an account with that email exists, a reset link has been sent.", null));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password using token",
            description = "Resets the user's password using the token received via email"
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully. You can now log in with your new password.", null));
    }

    // ──────────────────────────────────────────────
    // Helper: Build Set-Cookie headers for auth tokens
    // ──────────────────────────────────────────────
    private HttpHeaders buildAuthCookieHeaders(String accessToken, String refreshToken) {
        HttpHeaders headers = new HttpHeaders();

        // Access token: httpOnly, Secure, SameSite=Lax, path=/
        ResponseCookie.ResponseCookieBuilder accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(accessTokenExpiry / 1000) // convert ms to seconds
                .sameSite("Lax");

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            accessCookie.domain(cookieDomain);
        }

        headers.add(HttpHeaders.SET_COOKIE, accessCookie.build().toString());

        // Refresh token: httpOnly, Secure, SameSite=Lax, restricted path
        if (refreshToken != null) {
            ResponseCookie.ResponseCookieBuilder refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/api/auth") // Only sent to auth endpoints
                    .maxAge(refreshTokenExpiry / 1000)
                    .sameSite("Lax");

            if (cookieDomain != null && !cookieDomain.isBlank()) {
                refreshCookie.domain(cookieDomain);
            }

            headers.add(HttpHeaders.SET_COOKIE, refreshCookie.build().toString());
        }

        return headers;
    }

    // ──────────────────────────────────────────────
    // Helper: Extract cookie value from request
    // ──────────────────────────────────────────────
    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}