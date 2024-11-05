package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Optional<String> extractUserId(String token) {
        return Optional.ofNullable(extractClaims(token).getSubject());
    }

    private Optional<String> extractUserName(String token) {
        return Optional.ofNullable(extractClaims(token)
                .get("name", String.class));
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
