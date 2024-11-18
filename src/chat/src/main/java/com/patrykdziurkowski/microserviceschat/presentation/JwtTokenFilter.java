package com.patrykdziurkowski.microserviceschat.presentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.patrykdziurkowski.microserviceschat.application.AuthenticationApiClient;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private final AuthenticationApiClient apiClient;

    public JwtTokenFilter(AuthenticationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Optional<String> tokenResult = extractToken(request);
        if (tokenResult.isPresent()
                && apiClient.sendTokenValidationRequest(tokenResult.orElseThrow())) {
            UserDetails principal = new User("user", "", new ArrayList<>());
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null,
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equalsIgnoreCase("jwt")) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
