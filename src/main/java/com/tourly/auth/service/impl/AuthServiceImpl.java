package com.tourly.auth.service.impl;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tourly.auth.dto.request.RegisterRequest;
import com.tourly.auth.dto.request.LoginRequest;
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
    public UserResponse registerUser(RegisterRequest request) throws Exception {

        // 1️⃣ Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email already exists");
        }

        // 2️⃣ Check if phone exists
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new Exception("Phone already exists");
        }

        // 3️⃣ Find role
        RoleName roleNameEnum;
        try {
            roleNameEnum = RoleName.valueOf(request.getRoleName());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid role: " + request.getRoleName());
        }

        Role role = roleRepository.findByName(roleNameEnum)
                .orElseThrow(() -> new Exception("Role not found: " + roleNameEnum));

        // 4️⃣ Create User entity
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setKycVerified(false);
        user.setRole(role); // assign role

        // 5️⃣ Save to DB
        userRepository.save(user);

        // 6️⃣ Return response
        return UserMapper.toResponse(user);
    }

    // ===========================
    // LOGIN USER
    // ===========================
    @Override
    public AuthResponse loginUser(LoginRequest request) throws Exception {

        Optional<User> userOpt = userRepository.findByEmail(request.getEmailOrPhone());

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPhone(request.getEmailOrPhone());
        }

        if (userOpt.isEmpty()) {
            throw new Exception("User not found");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new Exception("Invalid password");
        }

        // update last login
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        // generate JWT token
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, UserMapper.toResponse(user));
    }
}