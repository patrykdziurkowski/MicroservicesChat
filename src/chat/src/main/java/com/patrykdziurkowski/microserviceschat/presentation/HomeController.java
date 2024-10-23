package com.patrykdziurkowski.microserviceschat.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "Placeholder text for the chat module.";
    }

    @GetMapping("/Placeholder")
    public String placeholder() {
        return "Placeholder endpoint for the chat module.";
    }
}