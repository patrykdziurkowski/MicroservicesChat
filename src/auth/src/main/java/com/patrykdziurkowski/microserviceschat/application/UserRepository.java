package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.User;

public interface UserRepository {
    List<User> get();

    Optional<User> getById(UUID id);

    Optional<User> getByUserName(String userName);

    void save(User user);

    void save(List<User> users);
}
