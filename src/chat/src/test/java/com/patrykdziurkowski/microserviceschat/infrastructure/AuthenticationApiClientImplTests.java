package com.patrykdziurkowski.microserviceschat.infrastructure;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;

@SpringBootTest(properties = {
        // disable the chat database for these tests to avoid loading them
        "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class AuthenticationApiClientImplTests {
    @Autowired
    private AuthenticationApiClientImpl apiClient;
    @SuppressWarnings("resource")
    @Container
    private static DockerComposeContainer<?> containers = new DockerComposeContainer<>(
            new File("../../docker-compose.yaml"))
            .waitingFor("auth", Wait.forHealthcheck())
            .withServices("db-auth", "auth")
            .withExposedService("auth", 8081)
            .withEnv("MSSQL_SA_PASSWORD", "exampleP@ssword123")
            .withEnv("JWT_SECRET", "8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz")
            .withBuild(true);

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        String hostname = containers.getServiceHost("auth", 8081);
        int port = containers.getServicePort("auth", 8081);
        String testUri = String.format("http://%s:%s", hostname, port);
        registry.add("auth.server.uri", () -> testUri);
    }

    @Test
    @Order(1)
    void apiClient_shouldLoad() {
        assertNotNull(apiClient);
    }

    @Test
    @Order(2)
    void sendLoginRequest_shouldReturnEmpty_whenNonExistantUser() {
        Optional<String> result = apiClient.sendLoginRequest("validUser", "P@ssword1!");

        assertTrue(result.isEmpty());
    }

    @Test
    @Order(3)
    void sendRegisterRequest_shouldReturnTrue_whenRegistered() throws Exception {
        boolean isSuccess = apiClient.sendRegisterRequest("validUser", "P@ssword1!");

        assertTrue(isSuccess);
    }

    @Test
    @Order(4)
    void sendRegisterRequest_shouldReturnFalse_whenNotRegisteredBecauseDuplicateUser() {
        boolean isSuccess = apiClient.sendRegisterRequest("validUser", "P@ssword1!");

        assertFalse(isSuccess);
    }

    @Test
    @Order(5)
    void sendLoginRequest_shouldReturnEmpty_whenWrongPassword() {
        Optional<String> result = apiClient.sendLoginRequest("validUser", "Password1!");

        assertTrue(result.isEmpty());
    }

    @Test
    @Order(6)
    void sendLoginRequest_shouldReturnToken_whenValidCredentials() {
        Optional<String> result = apiClient.sendLoginRequest("validUser", "P@ssword1!");

        assertTrue(result.isPresent());
        assertNotNull(result.orElseThrow());
        assertTrue(result.orElseThrow().length() > 0);
    }

    @Test
    @Order(7)
    void sendTokenValidationRequest_shouldReturnFalse_givenInvalidToken() {
        boolean isSuccess = apiClient.sendTokenValidationRequest("invalidToken123@");

        assertFalse(isSuccess);
    }

    @Test
    @Order(8)
    void sendTokenValidationRequest_shouldReturnTrue_givenValidToken() {
        Optional<String> result = apiClient.sendLoginRequest("validUser", "P@ssword1!");
        String token = result.orElseThrow();

        boolean isSuccess = apiClient.sendTokenValidationRequest(token);

        assertNotNull(token);
        assertTrue(isSuccess);
    }
}
