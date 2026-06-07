package com.tourly.auth.config;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class AdminPasswordInitializer {

    @Bean
    CommandLineRunner initAdminPassword(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> adminOpt = userRepository.findByEmail("admin");
            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();
                String hashed = passwordEncoder.encode("SuperAdmin@2026");
                admin.setPassword(hashed);
                userRepository.save(admin);
                System.out.println("✅ Admin password set successfully (BCrypt hash applied)");
            } else {
                System.out.println("⚠️ Admin user with email 'admin' not found");
            }
        };
    }
}
