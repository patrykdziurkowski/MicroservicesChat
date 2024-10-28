package com.patrykdziurkowski.microserviceschat.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.User;
import com.patrykdziurkowski.microserviceschat.infrastructure.UserRepository;

@Service
public class RegisterCommand {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterCommand(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean execute(String userName, String rawPassword) {
        if (userNameTaken(userName)) {
            return false;
        }

        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(userName, hashedPassword);
        userRepository.save(user);
        return true;
    }

    private boolean userNameTaken(String userName) {
        return userRepository.getByUserName(userName).isPresent();
    }
}
