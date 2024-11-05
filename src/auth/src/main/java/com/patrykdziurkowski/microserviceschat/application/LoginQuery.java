package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.User;

@Service
public class LoginQuery {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginQuery(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean execute(String userName, String rawPassword) {
        Optional<User> user = userRepository.getByUserName(userName);
        if (user.isEmpty()) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, user.get().getPasswordHash());
    }
}
