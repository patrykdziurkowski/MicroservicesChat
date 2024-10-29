package com.patrykdziurkowski.microserviceschat.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.LoginQuery;
import com.patrykdziurkowski.microserviceschat.application.RegisterCommand;

import jakarta.validation.Valid;

@RestController
public class UserController {
    private RegisterCommand registerCommand;
    private LoginQuery loginQuery;
    private JwtTokenManager jwtTokenManager;

    public UserController(RegisterCommand registerCommand,
            LoginQuery loginQuery,
            JwtTokenManager jwtTokenManager) {
        this.registerCommand = registerCommand;
        this.loginQuery = loginQuery;
        this.jwtTokenManager = jwtTokenManager;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserModel userData) {
        boolean isRegisterSuccessful = registerCommand.execute(userData.getUserName(), userData.getPassword());
        if (isRegisterSuccessful == false) {
            return new ResponseEntity<>("Registration was not successful.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("/login", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid UserModel userData) {
        boolean isLoginSuccessful = loginQuery.execute(userData.getUserName(), userData.getPassword());
        if (isLoginSuccessful == false) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String token = jwtTokenManager.generateToken(userData.getUserName());
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(token);
    }

}
