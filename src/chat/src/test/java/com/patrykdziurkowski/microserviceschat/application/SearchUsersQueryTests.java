package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

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
class SearchUsersQueryTests extends ComposeContainersBase {
    @Autowired
    private SearchUsersQuery usersQuery;
    @Autowired
    private AuthenticationApiClient apiClient;

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
    void usersQueryCommand_shouldLoad() {
        assertNotNull(usersQuery);
    }

    @Test
    @Order(2)
    void settingUpAUser_shouldWork() {
        boolean isRegistered = apiClient.sendRegisterRequest("searchUser1", "P@ssword1!");

        assertTrue(isRegistered);
    }

    @Test
    @Order(3)
    void execute_shouldReturnAtLeastOneUser_whenNoFilter() {
        List<User> users = usersQuery.execute(20, 0, Optional.empty()).orElseThrow();

        assertTrue(users.size() > 0);
    }

    @Test
    @Order(4)
    void execute_shouldReturnNoUsers_whenFilteredButNotFound() {
        List<User> users = usersQuery.execute(20, 0, Optional.of("@1@!")).orElseThrow();

        assertTrue(users.isEmpty());
    }

    @Test
    @Order(5)
    void execute_shouldReturnNoUsers_whenFilteredButFound() {
        List<User> users = usersQuery.execute(20, 0, Optional.of("User")).orElseThrow();

        assertTrue(users.size() > 0);
    }
}
