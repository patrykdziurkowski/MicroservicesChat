package com.patrykdziurkowski.microserviceschat.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ChangeUserNameCommand {
    private final AuthenticationApiClient apiClient;

    public ChangeUserNameCommand(AuthenticationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public boolean execute(UUID currentUserId, String newUserName) {
        return apiClient.sendUserNameChangeRequest(currentUserId, newUserName);
    }
}
