package com.patrykdziurkowski.microserviceschat.application.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.UserRepository;
import com.patrykdziurkowski.microserviceschat.domain.User;

@Service
public class MembersQuery {
    private final UserRepository userRepository;

    public MembersQuery(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> execute(List<UUID> userIds) {
        List<User> users = new ArrayList<>();
        for (UUID id : userIds) {
            Optional<User> user = userRepository.getById(id);
            if (user.isEmpty()) {
                continue;
            }

            users.add(user.orElseThrow());
        }
        return users;
    }
}
