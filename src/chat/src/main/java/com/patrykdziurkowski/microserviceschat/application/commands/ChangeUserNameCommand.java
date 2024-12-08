package com.patrykdziurkowski.microserviceschat.application.commands;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.UserApiClient;

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
