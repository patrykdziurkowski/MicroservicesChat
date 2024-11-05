package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenManager {
    @Value("${jwt.secret}")
    private String jwtSecret;
    private static final int ONE_HOUR_IN_MILLISECONDS = 1000 * 60 * 60;

    public String generateToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ONE_HOUR_IN_MILLISECONDS))
                .signWith(key)
                .compact();
    }
}
