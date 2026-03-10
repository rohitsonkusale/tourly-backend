package com.tourly.auth.controller;

import org.springframework.web.bind.annotation.*;

import com.tourly.auth.dto.request.RegisterRequest;
import com.tourly.auth.dto.request.LoginRequest;
import com.tourly.auth.dto.response.UserResponse;
import com.tourly.auth.dto.response.AuthResponse;
import com.tourly.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ===========================
    // REGISTER
    // ===========================
    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest request) throws Exception {
        return authService.registerUser(request);
    }

    // ===========================
    // LOGIN
    // ===========================
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) throws Exception {
        return authService.loginUser(request);
    }
}