package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

public interface UserApiClient {
    Optional<String> sendUserNameRequest(UUID userId);

    boolean sendUserNameChangeRequest(UUID userId, String newUserName);
}
