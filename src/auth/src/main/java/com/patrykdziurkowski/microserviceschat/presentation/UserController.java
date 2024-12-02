package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.ChangeUserNameCommand;
import com.patrykdziurkowski.microserviceschat.application.LoginQuery;
import com.patrykdziurkowski.microserviceschat.application.MembersQuery;
import com.patrykdziurkowski.microserviceschat.application.RegisterCommand;
import com.patrykdziurkowski.microserviceschat.application.UserQuery;
import com.patrykdziurkowski.microserviceschat.application.UsersQuery;
import com.patrykdziurkowski.microserviceschat.domain.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
public class UserController {
    private final RegisterCommand registerCommand;
    private final LoginQuery loginQuery;
    private final UserQuery userQuery;
    private final UsersQuery usersQuery;
    private final MembersQuery membersQuery;
    private final ChangeUserNameCommand changeUserNameCommand;
    private final JwtTokenManager jwtTokenManager;

    public UserController(RegisterCommand registerCommand,
            LoginQuery loginQuery,
            UserQuery userQuery,
            UsersQuery usersQuery,
            MembersQuery membersQuery,
            ChangeUserNameCommand changeUserNameCommand,
            JwtTokenManager jwtTokenManager) {
        this.registerCommand = registerCommand;
        this.loginQuery = loginQuery;
        this.userQuery = userQuery;
        this.usersQuery = usersQuery;
        this.membersQuery = membersQuery;
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
    public ResponseEntity<String> changeUserName(@RequestBody @Valid UserNameModel userData) {
        boolean isSuccess = changeUserNameCommand.execute(
                userData.getUserId(), userData.getUserName());
        if (isSuccess == false) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<GetUserModel> getUser(@PathVariable UUID userId) {
        Optional<User> user = userQuery.execute(userId);
        if (user.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        GetUserModel modelToReturn = new GetUserModel(user.get().getId(), user.get().getUserName());
        return ResponseEntity.ok(modelToReturn);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam @Valid @Min(value = 1) @Max(value = 20) int number,
            @RequestParam @Valid @Min(value = 0) int offset,
            @RequestParam(required = false) @Valid @Size(min = 1, max = 15) String filter) {
        List<User> users = usersQuery.execute(number, offset, Optional.ofNullable(filter));
        List<UserDto> userDtos = UserDto.fromList(users);

        return new ResponseEntity<>(
                userDtos,
                HttpStatus.OK);
    }

    @PostMapping("/members")
    public ResponseEntity<List<UserDto>> getMembers(@RequestBody List<UUID> memberIds) {
        List<User> members = membersQuery.execute(memberIds);

        return new ResponseEntity<>(UserDto.fromList(members), HttpStatus.OK);
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
