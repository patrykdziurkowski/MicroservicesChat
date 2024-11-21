package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.ChangeUserNameCommand;
import com.patrykdziurkowski.microserviceschat.application.LoginQuery;
import com.patrykdziurkowski.microserviceschat.application.RegisterCommand;
import com.patrykdziurkowski.microserviceschat.application.UserQuery;
import com.patrykdziurkowski.microserviceschat.domain.User;

import jakarta.validation.Valid;

@RestController
public class UserController {
    private RegisterCommand registerCommand;
    private LoginQuery loginQuery;
    private UserQuery userQuery;
    private ChangeUserNameCommand changeUserNameCommand;
    private JwtTokenManager jwtTokenManager;

    public UserController(RegisterCommand registerCommand,
            LoginQuery loginQuery,
            UserQuery userQuery,
            ChangeUserNameCommand changeUserNameCommand,
            JwtTokenManager jwtTokenManager) {
        this.registerCommand = registerCommand;
        this.loginQuery = loginQuery;
        this.userQuery = userQuery;
        this.changeUserNameCommand = changeUserNameCommand;
        this.jwtTokenManager = jwtTokenManager;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserModel userData) {
        boolean isRegisterSuccessful = registerCommand.execute(userData.getUserName(), userData.getPassword());
        if (isRegisterSuccessful == false) {
            return new ResponseEntity<>("Registration was not successful.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("/login", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid UserModel userData) {
        Optional<User> result = loginQuery.execute(userData.getUserName(), userData.getPassword());
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User user = result.get();
        String token = jwtTokenManager.generateToken(user.getId(), user.getUserName());
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(token);
    }

    @GetMapping("/authenticate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        Optional<UserClaims> result = authenticate(authorizationHeader);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(result.orElseThrow().getId().toString(), HttpStatus.OK);
    }

    @PutMapping("/username")
    public ResponseEntity<String> changeUserName(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody @Valid UserNameModel userData) {
        Optional<UserClaims> result = authenticate(authorizationHeader);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        boolean isSuccess = changeUserNameCommand.execute(
                result.get().getId(), result.get().getUserName());
        if (isSuccess == false) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<GetUserModel> getUser(@PathVariable UUID userId) {
        Optional<User> user = userQuery.execute(userId);
        if(user.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        GetUserModel modelToReturn = new GetUserModel(user.get().getId(), user.get().getUserName());
        return ResponseEntity.ok(modelToReturn);
    }

    private Optional<UserClaims> authenticate(String authorizationHeader) {
        if (authorizationHeader == null
                || authorizationHeader.startsWith("Bearer ") == false) {
            return Optional.empty();
        }

        String jwtToken = authorizationHeader.substring(7);
        return jwtTokenManager.validateToken(jwtToken);
    }
}
