package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.User;

@Service
public class UsersQuery {
    private final UserRepository userRepository;

    public UsersQuery(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> execute(int number, int offset, Optional<String> filter) {
        if (filter.isPresent()) {
            return userRepository.getByNumber(number, offset, filter.orElseThrow());
        }

        return userRepository.getByNumber(number, offset);
    }
}
