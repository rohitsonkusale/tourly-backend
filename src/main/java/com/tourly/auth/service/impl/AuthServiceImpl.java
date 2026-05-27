package com.tourly.auth.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tourly.auth.dto.request.LoginRequest;
import com.tourly.auth.dto.request.RegisterRequest;
import com.tourly.auth.dto.response.AuthResponse;
import com.tourly.auth.dto.response.UserResponse;
import com.tourly.auth.entity.AccountStatus;
import com.tourly.auth.entity.Role;
import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.auth.mapper.UserMapper;
import com.tourly.auth.repository.RoleRepository;
import com.tourly.auth.repository.UserRepository;
import com.tourly.auth.service.AuthService;
import com.tourly.auth.service.JwtService;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ConflictException;
import com.tourly.common.exception.ResourceNotFoundException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ===========================
    // REGISTER USER
    // ===========================
    @Override
    public UserResponse registerUser(RegisterRequest request) {

        // 1️⃣ Normalize input
        String fullName = request.getFullName().trim();
        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone().trim();
        String roleNameInput = request.getRoleName().trim().toUpperCase();

        // 2️⃣ Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        // 3️⃣ Check if phone already exists
        if (userRepository.existsByPhone(phone)) {
            throw new ConflictException("Phone number is already registered");
        }

        // 4️⃣ Validate and fetch role
        RoleName roleNameEnum;
        try {
            roleNameEnum = RoleName.valueOf(roleNameInput);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRoleName());
        }

        Role role = roleRepository.findByName(roleNameEnum)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleNameEnum));

        // 5️⃣ Create User entity
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setKycVerified(false);
        user.setRole(role);

        // 6️⃣ Save to DB
        User savedUser = userRepository.save(user);

        // 7️⃣ Return response
        return UserMapper.toResponse(savedUser);
    }

    // ===========================
    // LOGIN USER
    // ===========================
    @Override
    public AuthResponse loginUser(LoginRequest request) {

        String loginInput = request.getEmailOrPhone().trim();

        Optional<User> userOpt = userRepository.findByEmail(loginInput.toLowerCase());

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPhone(loginInput);
        }

        // Show friendly user feedback if account does not exist
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Email or phone is not registered. Please sign up!");
        }

        User user = userOpt.get();

        // Block non-active accounts
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException(
                    "Your account is not active. Current status: " + user.getAccountStatus().name()
            );
        }

        // Show specific feedback for incorrect passwords
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect password. Please try again.");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, UserMapper.toResponse(user));
    }
}