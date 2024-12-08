package com.patrykdziurkowski.microserviceschat.application.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.application.models.User;

public interface UserApiClient {
    Optional<String> sendUserNameRequest(UUID userId);

    boolean sendUserNameChangeRequest(UUID userId, String newUserName);

    Optional<List<User>> getUsers(int number, int offset, String filter);

    Optional<List<User>> getUsers(int number, int offset);

    Optional<List<User>> getMembers(List<UUID> userIds);
}
