package com.patrykdziurkowski.microserviceschat.presentation;

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

    @GetMapping("/chat")
    public String chats(Model model) {
        model.addAttribute("title", "Test title changed");
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
