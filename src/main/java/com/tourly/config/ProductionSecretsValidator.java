package com.tourly.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that all critical secrets are properly configured in production.
 * Fails fast at startup if any required secret is missing or still using a placeholder.
 */
@Component
@Profile("prod")
public class ProductionSecretsValidator {

    private static final Logger log = LoggerFactory.getLogger(ProductionSecretsValidator.class);

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${razorpay.key:}")
    private String razorpayKey;

    @Value("${razorpay.secret:}")
    private String razorpaySecret;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Value("${google.client-secret:}")
    private String googleClientSecret;

    @EventListener(ApplicationReadyEvent.class)
    public void validateSecrets() {
        List<String> errors = new ArrayList<>();

        // JWT Secret validation
        if (isBlankOrPlaceholder(jwtSecret)) {
            errors.add("JWT_SECRET is missing or uses a placeholder value");
        } else if (jwtSecret.length() < 32) {
            errors.add("JWT_SECRET is too short (minimum 32 characters for 256-bit security)");
        }

        // Database password
        if (isBlankOrPlaceholder(dbPassword)) {
            errors.add("DB_PASSWORD is missing");
        }

        // Razorpay
        if (isBlankOrPlaceholder(razorpayKey)) {
            errors.add("RAZORPAY_KEY is missing");
        } else if (razorpayKey.contains("test")) {
            log.warn("⚠️  RAZORPAY_KEY appears to be a test key — ensure this is intentional for staging");
        }

        if (isBlankOrPlaceholder(razorpaySecret)) {
            errors.add("RAZORPAY_SECRET is missing");
        }

        // Google OAuth
        if (isBlankOrPlaceholder(googleClientId)) {
            log.warn("⚠️  GOOGLE_CLIENT_ID is missing — Google SSO will be disabled");
        }
        if (isBlankOrPlaceholder(googleClientSecret)) {
            log.warn("⚠️  GOOGLE_CLIENT_SECRET is missing — Google SSO will be disabled");
        }

        if (!errors.isEmpty()) {
            String message = String.format(
                    "\n\n========================================\n" +
                    "🚨 PRODUCTION SECRETS VALIDATION FAILED\n" +
                    "========================================\n" +
                    "The following critical secrets are not properly configured:\n\n" +
                    "  • %s\n\n" +
                    "Set these as environment variables or in your deployment config.\n" +
                    "See .env.example for reference.\n" +
                    "========================================\n",
                    String.join("\n  • ", errors)
            );
            log.error(message);
            throw new IllegalStateException("Production secrets validation failed. " +
                    "Missing: " + String.join(", ", errors));
        }

        log.info("✅ All production secrets validated successfully");
    }

    private boolean isBlankOrPlaceholder(String value) {
        if (value == null || value.isBlank()) return true;
        String lower = value.toLowerCase();
        return lower.contains("your_") ||
               lower.contains("placeholder") ||
               lower.contains("change_me") ||
               lower.contains("supersecure") ||
               lower.equals("tourly@123");
    }
}
