package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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
class MembersQueryTests extends ComposeContainersBase {
    @Autowired
    private MembersQuery membersQuery;
    @Autowired
    private AuthenticationApiClientImpl apiClient;

    private static UUID member1Id;
    private static UUID member2Id;

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
        assertNotNull(membersQuery);
    }

    @Test
    @Order(2)
    void setup_shouldRegisterTwoUsers() {
        boolean isRegistered1 = apiClient.sendRegisterRequest("memberUser543x1", "P@ssword1!");
        String token1 = apiClient.sendLoginRequest("memberUser543x1", "P@ssword1!").orElseThrow();
        member1Id = apiClient.sendTokenValidationRequest(token1).orElseThrow();

        boolean isRegistered2 = apiClient.sendRegisterRequest("memberUser543x2", "P@ssword1!");
        String token2 = apiClient.sendLoginRequest("memberUser543x2", "P@ssword1!").orElseThrow();
        member2Id = apiClient.sendTokenValidationRequest(token2).orElseThrow();

        assertTrue(isRegistered1);
        assertTrue(isRegistered2);
    }

    @Test
    @Order(3)
    void execute_shouldReturnBothUserNames_whenBothIdsPassed() {
        List<User> users = membersQuery.execute(List.of(member1Id, member2Id)).orElseThrow();

        assertEquals("memberUser543x1", users.get(0).getUserName());
        assertEquals("memberUser543x2", users.get(1).getUserName());
    }
}
