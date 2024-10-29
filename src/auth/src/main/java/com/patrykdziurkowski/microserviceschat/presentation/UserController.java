package com.patrykdziurkowski.microserviceschat.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.RegisterCommand;

import jakarta.validation.Valid;

@RestController
public class UserController {
    private RegisterCommand registerCommand;

    public UserController(RegisterCommand registerCommand) {
        this.registerCommand = registerCommand;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserModel userData) {
        boolean isRegisterSuccessful = registerCommand.execute(userData.getUserName(), userData.getPassword());
        if (isRegisterSuccessful == false) {
            return new ResponseEntity<>("Registration was not successful.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("/login", HttpStatus.CREATED);
    }

}
