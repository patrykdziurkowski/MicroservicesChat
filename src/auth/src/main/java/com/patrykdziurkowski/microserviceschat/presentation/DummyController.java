package com.patrykdziurkowski.microserviceschat.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {
    @GetMapping("/")
    public String test() {
        return "Test dummy string for auth module.";
    }
}
