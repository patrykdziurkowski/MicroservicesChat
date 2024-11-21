package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.patrykdziurkowski.microserviceschat.application.AuthenticationApiClient;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class AuthenticationController {
    private final AuthenticationApiClient apiClient;

    public AuthenticationController(AuthenticationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute @Valid UserModel userData) {
        boolean isRegistered = apiClient.sendRegisterRequest(userData.getUserName(), userData.getPassword());
        if (isRegistered == false) {
            return "redirect:/register?error=true";
        }
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String login(
            @ModelAttribute @Valid UserModel userData,
            HttpServletResponse response) {
        final int HOUR_IN_SECONDS = 3600;
        Optional<String> tokenResult = apiClient.sendLoginRequest(userData.getUserName(), userData.getPassword());
        if (tokenResult.isEmpty()) {
            return "redirect:/login?error=true";
        }

        Cookie cookie = new Cookie("jwt", tokenResult.orElseThrow());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(HOUR_IN_SECONDS);
        response.addCookie(cookie);

        return "redirect:/chats";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();

        return "redirect:/";
    }

}
