package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.ChangeUserNameCommand;

import jakarta.validation.Valid;

@RestController
public class UserController {
    private final ChangeUserNameCommand changeUserName;

    public UserController(ChangeUserNameCommand changeuserName) {
        this.changeUserName = changeuserName;
    }

    @PutMapping("/username")
    public ResponseEntity<String> changeUserName(
            Authentication authentication,
            @RequestBody @Valid ChangeUserNameModel model) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        boolean nameChangeSuccess = changeUserName.execute(currentUserId, model.getUserName());
        if (nameChangeSuccess == false) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
