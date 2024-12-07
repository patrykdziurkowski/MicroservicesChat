package com.patrykdziurkowski.microserviceschat.application.queries;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.UserApiClient;
import com.patrykdziurkowski.microserviceschat.application.models.User;

@Service
public class MembersQuery {
    private final UserApiClient apiClient;

    public MembersQuery(UserApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Optional<List<User>> execute(List<UUID> userIds) {
        return apiClient.getMembers(userIds);
    }
}
