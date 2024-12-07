package com.patrykdziurkowski.microserviceschat.application.commands;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.patrykdziurkowski.microserviceschat.domain.User;
import com.patrykdziurkowski.microserviceschat.infrastructure.UserRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.presentation.AuthApplication;
import com.patrykdziurkowski.microserviceschat.presentation.AuthDbContainerBase;

@SpringBootTest
@Rollback
@Transactional
@ContextConfiguration(classes = AuthApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RegisterCommandTests extends AuthDbContainerBase {
    @Autowired
    private RegisterCommand registerCommand;
    @Autowired
    private UserRepositoryImpl userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerCommand_shouldLoad() {
        assertNotNull(registerCommand);
    }

    @Test
    void execute_shouldCreateNewUser_ifNotExists() {
        boolean isSuccess = registerCommand.execute("userName123", "rawPassword");

        assertTrue(isSuccess);
        List<User> users = userRepository.getByNumber(20, 0);
        assertEquals(1, users.size());
    }

    @Test
    void execute_shouldNotCreateNewUser_ifExists() {
        User firstUser = new User("userName123", "someOtherPwd123@");
        userRepository.save(firstUser);

        boolean isSuccess = registerCommand.execute("userName123", "rawPassword");

        assertFalse(isSuccess);
        List<User> users = userRepository.getByNumber(20, 0);
        assertEquals(1, users.size());
    }

    @Test
    void execute_shouldHashUserPassword_whenRegistered() {
        boolean isSuccess = registerCommand.execute("userName123", "rawPassword");

        List<User> users = userRepository.getByNumber(20, 0);
        User createdUser = users.get(0);
        assertTrue(isSuccess);
        assertEquals(1, users.size());
        assertNotEquals("rawPassword", createdUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("rawPassword", createdUser.getPasswordHash()));
    }
}
