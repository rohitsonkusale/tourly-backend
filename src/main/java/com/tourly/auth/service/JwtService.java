package com.tourly.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.tourly.auth.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // ===========================
    // GENERATE JWT TOKEN
    // ===========================
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole() != null && user.getRole().getName() != null
                ? user.getRole().getName().name()
                : null);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail()) // principal identity
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    // ===========================
    // EXTRACT ALL CLAIMS
    // ===========================
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ===========================
    // GENERIC CLAIM EXTRACTOR
    // ===========================
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ===========================
    // EXTRACT SUBJECT (EMAIL)
    // ===========================
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ===========================
    // EXTRACT EMAIL (same as subject)
    // ===========================
    public String extractEmail(String token) {
        return extractUsername(token);
    }

    // ===========================
    // EXTRACT USER ID
    // ===========================
    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");

        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof String) {
            return Long.parseLong((String) userId);
        }

        return null;
    }

    // ===========================
    // EXTRACT ROLE
    // ===========================
    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    // ===========================
    // EXTRACT EXPIRATION
    // ===========================
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ===========================
    // CHECK IF TOKEN EXPIRED
    // ===========================
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ===========================
    // VALIDATE TOKEN AGAINST USERDETAILS
    // ===========================
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // ===========================
    // BASIC TOKEN VALIDATION
    // (optional utility)
    // ===========================
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }
}