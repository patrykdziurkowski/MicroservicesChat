package com.patrykdziurkowski.microserviceschat.application.queries;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.UserRepository;
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
