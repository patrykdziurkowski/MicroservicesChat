package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.patrykdziurkowski.microserviceschat.application.UserApiClient;

@Controller
public class HomeController {
    private final UserApiClient apiClient;

    public HomeController(UserApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }

    @GetMapping("/chats")
    public String chats(
            Authentication authentication,
            Model model) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        String currentUserName = apiClient.sendUserNameRequest(currentUserId).orElse("");

        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUserName", currentUserName);
        return "chats";
    }

    @GetMapping("/chats/{chatId}")
    public String chat(
            @PathVariable String chatId,
            Authentication authentication,
            Model model) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        String currentUserName = apiClient.sendUserNameRequest(currentUserId).orElse("");

        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUserName", currentUserName);
        return "chat";
    }

}
