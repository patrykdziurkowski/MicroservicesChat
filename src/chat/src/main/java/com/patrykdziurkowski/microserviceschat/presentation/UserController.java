package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.ChangeUserNameCommand;
import com.patrykdziurkowski.microserviceschat.application.SearchUsersQuery;
import com.patrykdziurkowski.microserviceschat.application.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
public class UserController {
    private final ChangeUserNameCommand changeUserName;
    private final SearchUsersQuery searchUsersQuery;

    public UserController(
            ChangeUserNameCommand changeuserName,
            SearchUsersQuery searchUsersQuery) {
        this.changeUserName = changeuserName;
        this.searchUsersQuery = searchUsersQuery;
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

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam @Valid @Min(value = 1) @Max(value = 20) int number,
            @RequestParam @Valid @Min(value = 0) int offset,
            @RequestParam(required = false) @Valid @Size(min = 1, max = 15) String filter) {
        List<User> users = searchUsersQuery.execute(number, offset, Optional.ofNullable(filter)).orElseThrow();
        List<UserDto> userDtos = UserDto.fromList(users);

        return new ResponseEntity<>(
                userDtos,
                HttpStatus.OK);
    }
}
