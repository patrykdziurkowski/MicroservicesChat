package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.User;

@Service
public class UserQuery {
    private final UserRepository userRepository;

    public UserQuery(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> execute(UUID userId) {
        return userRepository.getById(userId);
    }
}
