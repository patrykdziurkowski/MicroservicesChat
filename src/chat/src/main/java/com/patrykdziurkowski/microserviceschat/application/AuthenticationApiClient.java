package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;

public interface AuthenticationApiClient {
    boolean sendRegisterRequest(String userName, String password);

    Optional<String> sendLoginRequest(String userName, String password);

    boolean sendTokenValidationRequest(String token);
}
