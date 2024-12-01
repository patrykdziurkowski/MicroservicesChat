package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrykdziurkowski.microserviceschat.application.User;
import com.patrykdziurkowski.microserviceschat.application.UserApiClient;
import com.patrykdziurkowski.microserviceschat.presentation.GetUserModel;

@Component
public class UserApiClientImpl implements UserApiClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${auth.server.uri}")
    private String authServerUri;

    public UserApiClientImpl(
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<String> sendUserNameRequest(UUID userId) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<GetUserModel> response = restTemplate.exchange(
                    authServerUri + "/users/" + userId,
                    HttpMethod.GET,
                    request,
                    GetUserModel.class);

            if (response.getStatusCode().is2xxSuccessful() == false
                    || response.getBody() == null) {
                return Optional.empty();
            }
            return Optional.of(response.getBody().getUserName());
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean sendUserNameChangeRequest(UUID currentUserId, String newUserName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("userId", currentUserId);
        body.put("userName", newUserName);
        try {
            HttpEntity<String> request = new HttpEntity<>(
                    objectMapper.writeValueAsString(body),
                    headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    authServerUri + "/username",
                    HttpMethod.PUT,
                    request,
                    String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            return false;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    @Override
    public Optional<List<User>> getUsers(int number, int offset, String filter) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<List<User>> response = restTemplate.exchange(
                    String.format("%s/users?number=%d&offset=%d&filter=%s", authServerUri, number, offset, filter),
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<User>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() == false) {
                return Optional.empty();
            }
            return Optional.of(response.getBody());
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<User>> getUsers(int number, int offset) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<List<User>> response = restTemplate.exchange(
                    String.format("%s/users?number=%d&offset=%d", authServerUri, number, offset),
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() == false) {
                return Optional.empty();
            }
            return Optional.of(response.getBody());
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

}
