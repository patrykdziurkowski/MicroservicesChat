package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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

    public Optional<String> sendUserNameRequest(UUID userId) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<GetUserModel> response = restTemplate.exchange(
                    authServerUri + "/users/" + userId,
                    HttpMethod.GET,
                    request,
                    GetUserModel.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody().getUserName());
            }
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

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
}
