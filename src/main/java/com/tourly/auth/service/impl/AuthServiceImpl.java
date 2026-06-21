package com.tourly.auth.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tourly.auth.dto.request.GoogleAuthRequest;
import com.tourly.auth.dto.request.LoginRequest;
import com.tourly.auth.dto.request.RegisterRequest;
import com.tourly.auth.dto.response.AuthResponse;
import com.tourly.auth.dto.response.UserResponse;
import com.tourly.auth.entity.AccountStatus;
import com.tourly.auth.entity.Role;
import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.common.entity.HostVerification;
import com.tourly.verification.entity.PlannerVerification;
import com.tourly.verification.enums.VerificationStatus;
import com.tourly.verification.repository.HostVerificationRepository;
import com.tourly.verification.repository.PlannerVerificationRepository;
import com.tourly.auth.mapper.UserMapper;
import com.tourly.auth.repository.RoleRepository;
import com.tourly.auth.repository.UserRepository;
import com.tourly.auth.service.AuthService;
import com.tourly.auth.service.JwtService;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ConflictException;
import com.tourly.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final HostVerificationRepository hostVerificationRepository;
    private final PlannerVerificationRepository plannerVerificationRepository;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           HostVerificationRepository hostVerificationRepository,
                           PlannerVerificationRepository plannerVerificationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hostVerificationRepository = hostVerificationRepository;
        this.plannerVerificationRepository = plannerVerificationRepository;
    }

    private void validateKycDetails(String aadhaarNumber, String panNumber) {
        if (aadhaarNumber == null || !aadhaarNumber.matches("^\\d{12}$")) {
            throw new BadRequestException("Invalid Aadhaar number. Must be exactly 12 digits.");
        }
        if (panNumber == null || !panNumber.toUpperCase().matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")) {
            throw new BadRequestException("Invalid PAN number format.");
        }
    }

    private void createKycVerification(User user, RoleName role) {
        if (role == RoleName.HOST) {
            HostVerification hostVer = new HostVerification();
            hostVer.setUser(user);
            hostVer.setDisplayName(user.getFullName());
            hostVer.setVerificationStatus(VerificationStatus.PENDING);
            hostVerificationRepository.save(hostVer);
        } else if (role == RoleName.PLANNER) {
            PlannerVerification plannerVer = new PlannerVerification();
            plannerVer.setUser(user);
            plannerVer.setDisplayName(user.getFullName());
            plannerVer.setVerificationStatus(VerificationStatus.PENDING);
            plannerVerificationRepository.save(plannerVer);
        }
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

        if (roleNameEnum == RoleName.HOST || roleNameEnum == RoleName.PLANNER) {
            validateKycDetails(request.getAadhaarNumber(), request.getPanNumber());
        }

        // 5️⃣ Create User entity
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setKycVerified(false);
        user.setRole(role);

        // Host/Planner accounts require admin approval before they can log in
        if (roleNameEnum == RoleName.HOST || roleNameEnum == RoleName.PLANNER) {
            // Store optional KYC fields if provided at signup (but don't require them)
            if (request.getAadhaarNumber() != null && !request.getAadhaarNumber().isBlank()) {
                user.setAadhaarNumber(request.getAadhaarNumber());
            }
            if (request.getPanNumber() != null && !request.getPanNumber().isBlank()) {
                user.setPanNumber(request.getPanNumber().toUpperCase());
            }
            if (request.getInstagramUsername() != null && !request.getInstagramUsername().isBlank()) {
                user.setInstagramUsername(request.getInstagramUsername());
            }
            if (request.getWebsiteUrl() != null && !request.getWebsiteUrl().isBlank()) {
                user.setWebsiteUrl(request.getWebsiteUrl());
            }
            user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        } else {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        // 6️⃣ Save to DB
        User savedUser = userRepository.save(user);

        // No auto KYC record creation — host/planner will submit KYC manually later

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
            throw new BadCredentialsException("You don't have an account yet. First signup then login");
        }

        User user = userOpt.get();

        // Block Host/Planner accounts that are not yet admin approved
        if ((user.getRole().getName() == RoleName.HOST || user.getRole().getName() == RoleName.PLANNER) && !Boolean.TRUE.equals(user.getAdminApproved())) {
            throw new BadRequestException(
                    "PENDING_VERIFICATION:Your Host/Planner request has already been submitted. " +
                    "Verification is still in process. It typically takes 7-8 hours to verify your details. " +
                    "Please wait until your account is approved by the admin."
            );
        }

        // Block non-active accounts
        if (user.getAccountStatus() != AccountStatus.ACTIVE && !Boolean.TRUE.equals(user.getAdminApproved())) {
            throw new BadRequestException(
                    "Your account is not active. Current status: " + user.getAccountStatus().name()
            );
        }

        // Show specific feedback for incorrect passwords
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect password. Please try again.");
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT tokens
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(token, refreshToken, UserMapper.toResponse(user));
    }

    // ===========================
    // GOOGLE OAUTH AUTHENTICATION
    // ===========================
    @Override
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        // 1. Verify Google ID token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(request.getCredential());
        } catch (Exception e) {
            throw new BadRequestException("Google ID token verification failed: " + e.getMessage());
        }

        if (idToken == null) {
            throw new BadRequestException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleId = payload.getSubject();
        String email = payload.getEmail().toLowerCase();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        // 2. Find existing user by Google ID or by email
        Optional<User> userOpt = userRepository.findByGoogleId(googleId);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(email);
        }

        if (userOpt.isEmpty()) {
            // User doesn't exist — create a new account (Google Signup flow)
            String roleName = request.getRole();
            if (roleName == null || roleName.isBlank()) {
                throw new BadCredentialsException("You don't have an account yet. First signup then login");
            }

            RoleName roleEnum;
            try {
                roleEnum = RoleName.valueOf(roleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + roleName);
            }

            Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));

            User newUser = new User();
            newUser.setFullName(name != null ? name : "Google User");
            newUser.setEmail(email);
            newUser.setGoogleId(googleId);
            newUser.setAvatar(pictureUrl);
            newUser.setRole(role);
            newUser.setAccountStatus(AccountStatus.ACTIVE);
            newUser.setEmailVerified(true);

            if (request.getPhone() != null && !request.getPhone().isBlank()) {
                newUser.setPhone(request.getPhone().replaceAll("\\D", ""));
            }
            if (request.getAadhaarNumber() != null && !request.getAadhaarNumber().isBlank()) {
                newUser.setAadhaarNumber(request.getAadhaarNumber());
            }
            if (request.getPanNumber() != null && !request.getPanNumber().isBlank()) {
                newUser.setPanNumber(request.getPanNumber().toUpperCase());
            }
            if (request.getInstagramUsername() != null && !request.getInstagramUsername().isBlank()) {
                newUser.setInstagramUsername(request.getInstagramUsername());
            }
            if (request.getWebsiteUrl() != null && !request.getWebsiteUrl().isBlank()) {
                newUser.setWebsiteUrl(request.getWebsiteUrl());
            }

            if (roleEnum == RoleName.HOST || roleEnum == RoleName.PLANNER) {
                newUser.setAdminApproved(false);
                newUser.setKycVerified(false);
                newUser.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
            } else {
                newUser.setAdminApproved(true);
            }

            newUser = userRepository.save(newUser);

            // No auto KYC record — host/planner will submit KYC manually later

            // Block Host/Planner from immediate login (pending admin approval)
            if (roleEnum == RoleName.HOST || roleEnum == RoleName.PLANNER) {
                throw new BadRequestException(
                        "PENDING_VERIFICATION:Your " + roleEnum.name().toLowerCase() + " account has been created! " +
                        "It typically takes 7-8 hours to verify. You can log in once approved by admin."
                );
            }

            newUser.setLastLoginAt(LocalDateTime.now());
            newUser = userRepository.save(newUser);
            String token = jwtService.generateToken(newUser);
            String refreshToken = jwtService.generateRefreshToken(newUser);
            return new AuthResponse(token, refreshToken, UserMapper.toResponse(newUser));
        }

        User user = userOpt.get();

        // Link Google ID if not already done
        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
        }

        // Sync avatar if missing
        if (user.getAvatar() == null) {
            user.setAvatar(pictureUrl);
        }

        // Block Host/Planner accounts that are not yet admin approved
        if ((user.getRole().getName() == RoleName.HOST || user.getRole().getName() == RoleName.PLANNER) && !Boolean.TRUE.equals(user.getAdminApproved())) {
            throw new BadRequestException(
                    "PENDING_VERIFICATION:Your Host/Planner request has already been submitted. " +
                    "Verification is still in process. It typically takes 7-8 hours to verify your details. " +
                    "Please wait until your account is approved by the admin."
            );
        }

        // Block non-active accounts
        if (user.getAccountStatus() != AccountStatus.ACTIVE && !Boolean.TRUE.equals(user.getAdminApproved())) {
            throw new BadRequestException(
                    "Your account is not active. Current status: " + user.getAccountStatus().name()
            );
        }

        user.setLastLoginAt(LocalDateTime.now());
        user = userRepository.save(user);

        // Generate JWT tokens (only for ACTIVE accounts — travelers)
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(token, refreshToken, UserMapper.toResponse(user));
    }

    // ===========================
    // REFRESH TOKEN
    // ===========================
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // 1. Validate it's actually a refresh token (works even if expired)
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid token. Please provide a valid refresh token.");
        }

        // 2. Check if it's expired
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Your session has expired. Please login again.");
        }

        // 3. Extract user email and find the user
        String email;
        try {
            email = jwtService.extractEmail(refreshToken);
        } catch (Exception e) {
            throw new BadRequestException("Invalid refresh token. Please login again.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Account not found. Please login again."));

        // 4. Block deleted accounts
        if (user.getDeletedAt() != null) {
            throw new BadRequestException("This account has been deleted.");
        }

        // 5. Block inactive/unapproved accounts
        if (user.getAccountStatus() != AccountStatus.ACTIVE && !Boolean.TRUE.equals(user.getAdminApproved())) {
            throw new BadRequestException("Your account is no longer active. Please contact support.");
        }

        // 6. Issue new access token + rotate refresh token
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken, UserMapper.toResponse(user));
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserMapper.toResponse(user);
    }
}