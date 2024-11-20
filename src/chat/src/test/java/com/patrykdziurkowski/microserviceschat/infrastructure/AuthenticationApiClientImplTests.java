package com.patrykdziurkowski.microserviceschat.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

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

import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;
import com.patrykdziurkowski.microserviceschat.presentation.ComposeContainersBase;

@SpringBootTest(properties = {
        // disable the chat database for these tests to avoid loading them
        "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class AuthenticationApiClientImplTests extends ComposeContainersBase {
    @Autowired
    private AuthenticationApiClientImpl apiClient;

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        String hostname = containers.getServiceHost("auth", 8081);
        int port = containers.getServicePort("auth", 8081);
        String testUri = String.format("http://%s:%s", hostname, port);
        registry.add("auth.server.uri", () -> testUri);
        registry.add("spring.datasource.password", () -> TEST_DB_PASSWORD);
    }

    @Test
    @Order(1)
    void apiClient_shouldLoad() {
        assertNotNull(apiClient);
    }

    @Test
    @Order(2)
    void sendLoginRequest_shouldReturnEmpty_whenNonExistantUser() {
        Optional<String> result = apiClient.sendLoginRequest("validUser",
                "P@ssword1!");

        assertTrue(result.isEmpty());
    }

    @Test
    @Order(3)
    void sendRegisterRequest_shouldReturnTrue_whenRegistered() {
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
        Optional<String> result = apiClient.sendLoginRequest("validUser",
                "Password1!");

        assertTrue(result.isEmpty());
    }

    @Test
    @Order(6)
    void sendLoginRequest_shouldReturnToken_whenValidCredentials() {
        Optional<String> result = apiClient.sendLoginRequest("validUser",
                "P@ssword1!");

        assertTrue(result.isPresent());
        assertNotNull(result.orElseThrow());
        assertTrue(result.orElseThrow().length() > 0);
    }

    @Test
    @Order(7)
    void sendTokenValidationRequest_shouldReturnFalse_givenInvalidToken() {
        Optional<UUID> userIdResult = apiClient.sendTokenValidationRequest("invalidToken123@");

        assertTrue(userIdResult.isEmpty());
    }

    @Test
    @Order(8)
    void sendTokenValidationRequest_shouldReturnTrue_givenValidToken() {
        Optional<String> result = apiClient.sendLoginRequest("validUser",
                "P@ssword1!");
        String token = result.orElseThrow();

        Optional<UUID> userIdResult = apiClient.sendTokenValidationRequest(token);

        assertNotNull(token);
        assertTrue(userIdResult.isPresent());
    }
}
