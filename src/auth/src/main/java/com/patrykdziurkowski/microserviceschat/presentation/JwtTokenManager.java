package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.patrykdziurkowski.microserviceschat.presentation.models.UserClaims;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenManager {
    @Value("${jwt.secret}")
    private String jwtSecret;
    private static final int ONE_HOUR_IN_MILLISECONDS = 1000 * 60 * 60;

    public String generateToken(UUID userId, String username) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("name", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ONE_HOUR_IN_MILLISECONDS))
                .signWith(getSignInKey())
                .compact();
    }

    public Optional<UserClaims> validateToken(String token) {
        if (isTokenExpired(token)
                || extractUserId(token).isEmpty()
                || extractUserName(token).isEmpty()) {
            return Optional.empty();
        }

        UUID userId = UUID.fromString(extractUserId(token).orElseThrow());
        String userName = extractUserName(token).orElseThrow();
        UserClaims userClaims = new UserClaims(userId, userName);
        return Optional.of(userClaims);
    }

    private boolean isTokenExpired(String token) {
        if (extractClaims(token).isEmpty()) {
            return true;
        }
        return extractClaims(token).orElseThrow().getExpiration().before(new Date());
    }

    private Optional<String> extractUserId(String token) {
        if (extractClaims(token).isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(extractClaims(token).orElseThrow().getSubject());
    }

    private Optional<String> extractUserName(String token) {
        if (extractClaims(token).isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(extractClaims(token).orElseThrow()
                .get("name", String.class));
    }

    private Optional<Claims> extractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
