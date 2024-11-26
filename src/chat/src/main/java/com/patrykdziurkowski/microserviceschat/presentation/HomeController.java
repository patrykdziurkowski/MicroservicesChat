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
    public String chats() {
        return "chats";
    }

    @GetMapping("/chats/{chatId}")
    public String chat(@PathVariable String chatId) {
        return "chat";
    }

}
