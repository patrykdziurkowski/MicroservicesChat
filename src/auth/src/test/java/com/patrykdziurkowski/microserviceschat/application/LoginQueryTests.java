package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.patrykdziurkowski.microserviceschat.AuthDbContainerBase;
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
class LoginQueryTests extends AuthDbContainerBase {
        @Autowired
        private LoginQuery loginQuery;
        @Autowired
        private RegisterCommand registerCommand;

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
