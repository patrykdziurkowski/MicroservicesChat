package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticationApiClient {
    boolean sendRegisterRequest(String userName, String password);

    Optional<String> sendLoginRequest(String userName, String password);

    Optional<UUID> sendTokenValidationRequest(String token);
}
