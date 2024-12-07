package com.patrykdziurkowski.microserviceschat.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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

import com.patrykdziurkowski.microserviceschat.application.models.User;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;
import com.patrykdziurkowski.microserviceschat.presentation.ComposeContainersBase;

@SpringBootTest(properties = {
        // disable the chat database for these tests to avoid loading them
        "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class UserApiClientImplTests extends ComposeContainersBase {
    @Autowired
    private UserApiClientImpl userApiClient;
    @Autowired
    private AuthenticationApiClientImpl authApiClient;

    private static UUID userId;

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
        assertNotNull(userApiClient);
    }

    @Test
    @Order(2)
    void sendUserNameRequest_shouldReturnUserName_whenValidUserId() {
        boolean isRegistered = authApiClient.sendRegisterRequest("validUser53", "P@ssword1!");
        Optional<String> result = authApiClient.sendLoginRequest("validUser53",
                "P@ssword1!");
        String token = result.orElseThrow();
        Optional<UUID> userIdResult = authApiClient.sendTokenValidationRequest(token);
        userId = userIdResult.orElseThrow();

        Optional<String> userNameResult = userApiClient.sendUserNameRequest(userIdResult.get());

        assertTrue(isRegistered);
        assertTrue(userNameResult.isPresent());
        assertEquals("validUser53", userNameResult.get());
    }

    @Test
    @Order(3)
    void sendUserNameRequest_shouldReturnEmpty_whenInvalidUserId() {
        Optional<String> userNameResult = userApiClient.sendUserNameRequest(UUID.randomUUID());

        assertTrue(userNameResult.isEmpty());
    }

    @Test
    @Order(4)
    void sendUserNameChangeRequest_shouldReturnFalse_whenNotChanged() {
        Optional<String> result = authApiClient.sendLoginRequest(
                "validUser53",
                "P@ssword1!");
        String token = result.orElseThrow();
        Optional<UUID> userIdResult = authApiClient.sendTokenValidationRequest(token);

        boolean changeResult = userApiClient.sendUserNameChangeRequest(
                userIdResult.orElseThrow(),
                "validUser53");

        String userNameAfterResult = userApiClient
                .sendUserNameRequest(userIdResult.orElseThrow()).orElseThrow();
        assertFalse(changeResult);
        assertEquals("validUser53", userNameAfterResult);
    }

    @Test
    @Order(5)
    void sendUserNameChangeRequest_shouldReturnTrue_whenChanged() {
        Optional<String> result = authApiClient.sendLoginRequest(
                "validUser53",
                "P@ssword1!");
        String token = result.orElseThrow();
        Optional<UUID> userIdResult = authApiClient.sendTokenValidationRequest(token);

        boolean changeResult = userApiClient.sendUserNameChangeRequest(
                userIdResult.orElseThrow(),
                "newUserName");

        String userNameAfterResult = userApiClient
                .sendUserNameRequest(userIdResult.orElseThrow()).orElseThrow();
        assertTrue(changeResult);
        assertEquals("newUserName", userNameAfterResult);
    }

    @Test
    @Order(6)
    void getUsers_shouldReturnUsers_whenFiltered() {
        List<User> users = userApiClient.getUsers(20, 0, "Name").orElseThrow();

        assertTrue(users.size() > 0);
    }

    @Test
    @Order(7)
    void getUsers_shouldReturnUsers_whenTheyExist() {
        List<User> users = userApiClient.getUsers(20, 0).orElseThrow();

        assertTrue(users.size() > 0);
    }

    @Test
    @Order(8)
    void getMembers_shouldReturnUsers_whenTheyExist() {
        List<User> users = userApiClient.getMembers(List.of(userId, userId)).orElseThrow();

        assertTrue(users.size() > 0);
    }

}
