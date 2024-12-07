package com.patrykdziurkowski.microserviceschat.application.queries;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.UserRepository;
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

    public Optional<User> execute(String userName, String rawPassword) {
        Optional<User> user = userRepository.getByUserName(userName);
        if (user.isEmpty()) {
            return Optional.empty();
        }

        if (passwordEncoder.matches(rawPassword,
                user.get().getPasswordHash()) == false) {
            return Optional.empty();
        }
        return user;
    }
}
