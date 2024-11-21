package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "Test title changed");
        model.addAttribute("message", "Test message");
        return "register";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Test title changed");
        model.addAttribute("message", "Test message");
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }
  
    @GetMapping("/chats")
    public String chats(Authentication authentication, Model model) {
        UUID userId = UUID.fromString(authentication.getName());
        model.addAttribute("title", userId);
        model.addAttribute("message", "Test message");
        return "chats";
    }

    @GetMapping("/chats/{chatId}")
    public String chat(
            @PathVariable String chatId,
            Model model) {
        model.addAttribute("title", "Chat " + chatId);
        model.addAttribute("message", "Test message");
        return "chat";
    }

}
