package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
