package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.patrykdziurkowski.microserviceschat.domain.User;
import com.patrykdziurkowski.microserviceschat.presentation.AuthApplication;

@SpringBootTest
@Rollback
@Transactional
@ContextConfiguration(classes = AuthApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class LoginQueryTests {
    @Autowired
    private LoginQuery loginQuery;
    @Autowired
    private RegisterCommand registerCommand;

    @SuppressWarnings("resource")
    @Container
    @ServiceConnection
    private static MSSQLServerContainer<?> db = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-CU15-GDR1-ubuntu-22.04")
            .withExposedPorts(1433)
            .waitingFor(Wait.forSuccessfulCommand(
                    "/opt/mssql-tools18/bin/sqlcmd -U sa -S localhost -P examplePassword123 -No -Q 'SELECT 1'"))
            .acceptLicense()
            .withPassword("examplePassword123");

    @Test
    void registerCommand_shouldLoad() {
        assertNotNull(loginQuery);
    }

    @Test
    void execute_givenNonExistentUser_returnsEmpty() {
        Optional<User> user = loginQuery.execute("fakeUser", "password123");

        assertTrue(user.isEmpty());
    }

    @Test
    void execute_givenExistingUserWithWrongPassword_returnsFalse() {
        boolean registrationIsSuccessful = registerCommand
                .execute("existingUser", "differentPasswordHash");

        Optional<User> user = loginQuery.execute("existingUser", "wrongPasswordHash");

        assertTrue(registrationIsSuccessful);
        assertTrue(user.isEmpty());
    }

    @Test
    void execute_givenExistingUserWithCorrectPassword_returnsTrue() {
        boolean isRegisterSuccess = registerCommand
                .execute("existingUser", "correctPasswordHash");

        Optional<User> user = loginQuery
                .execute("existingUser", "correctPasswordHash");

        assertTrue(isRegisterSuccess);
        assertTrue(user.isPresent());
    }
}
