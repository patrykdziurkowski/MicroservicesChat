package com.patrykdziurkowski.microserviceschat.application;

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

import com.patrykdziurkowski.microserviceschat.infrastructure.AuthenticationApiClientImpl;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;
import com.patrykdziurkowski.microserviceschat.presentation.ComposeContainersBase;

@SpringBootTest(properties = {
        // disable the chat database for these tests to avoid loading them
        "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class ChangeUserNameCommandTests extends ComposeContainersBase {
    @Autowired
    private ChangeUserNameCommand command;
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
    void command_shouldLoad() {
        assertNotNull(command);
    }

    @Test
    @Order(2)
    void setup_setupValidUser_whenValidData() {
        boolean isRegistered = apiClient.sendRegisterRequest("validUser", "P@ssword1!");
        Optional<String> tokenResult = apiClient.sendLoginRequest("validUser", "P@ssword1!");

        assertTrue(isRegistered);
        assertTrue(tokenResult.orElseThrow().length() > 0);
    }

    @Test
    @Order(3)
    void command_shouldNotChangeUserName_whenDuplicate() {
        Optional<String> tokenResult = apiClient.sendLoginRequest("validUser", "P@ssword1!");
        Optional<UUID> userIdResult = apiClient.sendTokenValidationRequest(tokenResult.orElseThrow());
        UUID userId = userIdResult.orElseThrow();

        boolean isSuccess = command.execute(userId, "validUser");
        assertFalse(isSuccess);
    }

    @Test
    @Order(4)
    void command_shouldChangeUserName_whenUnique() {
        Optional<String> tokenResult = apiClient.sendLoginRequest("validUser", "P@ssword1!");
        Optional<UUID> userIdResult = apiClient.sendTokenValidationRequest(tokenResult.orElseThrow());
        UUID userId = userIdResult.orElseThrow();

        boolean isSuccess = command.execute(userId, "newUserName5");
        assertTrue(isSuccess);
    }
}
