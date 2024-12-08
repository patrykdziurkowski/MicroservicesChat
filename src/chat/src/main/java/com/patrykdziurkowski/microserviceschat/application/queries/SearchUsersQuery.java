package com.patrykdziurkowski.microserviceschat.application.queries;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.UserApiClient;
import com.patrykdziurkowski.microserviceschat.application.models.User;

@Service
public class SearchUsersQuery {
    private final UserApiClient apiClient;

    public SearchUsersQuery(UserApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Optional<List<User>> execute(int number, int offset, Optional<String> filter) {
        if (filter.isPresent()) {
            return apiClient.getUsers(number, offset, filter.orElseThrow());
        }

        return apiClient.getUsers(number, offset);
    }
}
