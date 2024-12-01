package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.patrykdziurkowski.microserviceschat.application.AuthenticationApiClient;

@Component
public class AuthenticationApiClientImpl implements AuthenticationApiClient {
    private final RestTemplate restTemplate;

    @Value("${auth.server.uri}")
    private String authServerUri;

    public AuthenticationApiClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean sendRegisterRequest(String userName, String password) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userName", userName);
        requestBody.put("password", password);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    authServerUri + "/register",
                    requestBody,
                    String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    public Optional<String> sendLoginRequest(String userName, String password) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userName", userName);
        requestBody.put("password", password);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    authServerUri + "/login",
                    requestBody,
                    String.class);
            if (response.getStatusCode().isError()) {
                return Optional.empty();
            }
            return Optional.of(response.getBody());
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    public Optional<UUID> sendTokenValidationRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    authServerUri + "/authenticate",
                    HttpMethod.GET,
                    request,
                    String.class);

            if (response.getStatusCode().isError()) {
                return Optional.empty();
            }
            return Optional.of(UUID.fromString(response.getBody()));
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

}