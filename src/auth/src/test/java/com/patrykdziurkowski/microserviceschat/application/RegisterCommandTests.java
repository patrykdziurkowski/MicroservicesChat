package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.patrykdziurkowski.microserviceschat.domain.User;
import com.patrykdziurkowski.microserviceschat.infrastructure.UserRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.presentation.AuthApplication;

@SpringBootTest
@Rollback
@Transactional
@ContextConfiguration(classes = AuthApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class RegisterCommandTests {
    @Autowired
    private RegisterCommand registerCommand;
    @Autowired
    private UserRepositoryImpl userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        assertNotNull(registerCommand);
    }

    @Test
    void execute_shouldCreateNewUser_ifNotExists() {
        boolean isSuccess = registerCommand.execute("userName123", "rawPassword");

        assertTrue(isSuccess);
        List<User> users = userRepository.get();
        assertEquals(1, users.size());
    }

    @Test
    void execute_shouldNotCreateNewUser_ifExists() {
        User firstUser = new User("userName123", "someOtherPwd123@");
        userRepository.save(firstUser);

        boolean isSuccess = registerCommand.execute("userName123", "rawPassword");

        assertFalse(isSuccess);
        List<User> users = userRepository.get();
        assertEquals(1, users.size());
    }

    @Test
    void execute_shouldHashUserPassword_whenRegistered()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        boolean isSuccess = registerCommand.execute("userName123", "rawPassword");

        List<User> users = userRepository.get();
        User createdUser = users.get(0);
        java.lang.reflect.Field passwordField = User.class.getDeclaredField("passwordHash");
        passwordField.setAccessible(true);
        String passwordInDatabase = (String) passwordField.get(createdUser);
        assertTrue(isSuccess);
        assertEquals(1, users.size());
        assertNotEquals(passwordInDatabase, "rawPassword");
        assertTrue(createdUser.login("userName123", passwordInDatabase));
        assertTrue(passwordEncoder.matches("rawPassword", passwordInDatabase));
    }
}
