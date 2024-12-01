package com.patrykdziurkowski.microserviceschat.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ChangeUserNameCommand {
    private final UserApiClient apiClient;

    public ChangeUserNameCommand(UserApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public boolean execute(UUID currentUserId, String newUserName) {
        return apiClient.sendUserNameChangeRequest(currentUserId, newUserName);
    }
}
