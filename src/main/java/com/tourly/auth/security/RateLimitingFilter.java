package com.tourly.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tourly.common.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate-limiting filter for authentication endpoints.
 * Protects /api/auth/login, /api/auth/register, and /api/auth/refresh
 * against brute-force attacks using an in-memory Caffeine cache.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Cache<String, AtomicInteger> requestCounts;
    private final int maxRequests;

    public RateLimitingFilter(
            @Value("${app.rate-limit.auth.requests-per-minute:10}") int maxRequestsPerMinute,
            @Value("${app.rate-limit.auth.block-duration-minutes:15}") int blockDurationMinutes) {
        this.maxRequests = maxRequestsPerMinute;
        this.requestCounts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(blockDurationMinutes))
                .maximumSize(10_000)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Only rate-limit POST requests to auth endpoints
        if (!"POST".equalsIgnoreCase(method)) return true;

        return !(path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/google"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientKey = getClientIdentifier(request);

        AtomicInteger counter = requestCounts.get(clientKey, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        if (currentCount > maxRequests) {
            log.warn("Rate limit exceeded for client: {} on path: {}", clientKey, request.getRequestURI());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");

            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "Too many requests. Please try again later.",
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            objectMapper.writeValue(response.getOutputStream(), errorResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Identify the client by IP address.
     * Respects X-Forwarded-For header for proxied requests.
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Take the first IP (original client)
            return xForwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
