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
 * Rate-limiting filter for sensitive endpoints.
 * Protects authentication and critical booking operations
 * against brute-force attacks using an in-memory Caffeine cache.
 *
 * Protected endpoints:
 * - /api/auth/login, /api/auth/register, /api/auth/refresh, /api/auth/google (POST) — 10 req/min
 * - /api/bookings/{id}/cancel (PUT) — 5 req/min
 * - /api/payments/refund/{id} (POST) — 5 req/min
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Cache<String, AtomicInteger> authRequestCounts;
    private final Cache<String, AtomicInteger> sensitiveRequestCounts;
    private final int maxAuthRequests;
    private final int maxSensitiveRequests;

    public RateLimitingFilter(
            @Value("${app.rate-limit.auth.requests-per-minute:10}") int maxAuthRequestsPerMinute,
            @Value("${app.rate-limit.auth.block-duration-minutes:15}") int blockDurationMinutes,
            @Value("${app.rate-limit.sensitive.requests-per-minute:5}") int maxSensitiveRequestsPerMinute) {
        this.maxAuthRequests = maxAuthRequestsPerMinute;
        this.maxSensitiveRequests = maxSensitiveRequestsPerMinute;

        this.authRequestCounts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(blockDurationMinutes))
                .maximumSize(10_000)
                .build();

        this.sensitiveRequestCounts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(10_000)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Auth endpoints (POST only)
        if ("POST".equalsIgnoreCase(method)) {
            if (path.equals("/api/auth/login")
                    || path.equals("/api/auth/register")
                    || path.equals("/api/auth/refresh")
                    || path.equals("/api/auth/google")) {
                return false;
            }
            // Refund request endpoint
            if (path.matches("/api/payments/refund/\\d+")) {
                return false;
            }
        }

        // Cancel booking endpoint (PUT)
        if ("PUT".equalsIgnoreCase(method) && path.matches("/api/bookings/\\d+/cancel")) {
            return false;
        }

        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientKey = getClientIdentifier(request);
        String path = request.getRequestURI();

        boolean isAuthEndpoint = path.startsWith("/api/auth/");
        Cache<String, AtomicInteger> cache = isAuthEndpoint ? authRequestCounts : sensitiveRequestCounts;
        int maxAllowed = isAuthEndpoint ? maxAuthRequests : maxSensitiveRequests;

        // For authenticated endpoints, use userId (from auth cookie/token) + IP as key
        // For auth endpoints (pre-login), use IP only
        String cacheKey;
        if (isAuthEndpoint) {
            cacheKey = clientKey;
        } else {
            // Try to extract authenticated user identity from security context
            String userId = getUserIdentifier(request);
            cacheKey = (userId != null ? userId : clientKey) + ":" + path.replaceAll("\\d+", "*");
        }

        AtomicInteger counter = cache.get(cacheKey, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        if (currentCount > maxAllowed) {
            log.warn("Rate limit exceeded for client: {} on path: {} (count: {}/{})",
                    cacheKey, path, currentCount, maxAllowed);

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
     * Try to extract user identity from the security context (set by JwtAuthenticationFilter).
     * Returns username/email if authenticated, null otherwise.
     */
    private String getUserIdentifier(HttpServletRequest request) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
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
