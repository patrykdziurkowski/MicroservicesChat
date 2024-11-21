package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.patrykdziurkowski.microserviceschat.domain.User;
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
class ChangeUserNameCommandTests extends AuthDbContainerBase {
    @Autowired
    private ChangeUserNameCommand changeUserNameCommand;
    @Autowired
    private RegisterCommand registerCommand;
    @Autowired
    private UserRepository userRepository;

    @Test
    void changeUserNameCommand_shouldLoad() {
        assertNotNull(changeUserNameCommand);
    }

    @Test
    void execute_shouldReturnFalse_whenNewUserNameIsAlreadyTaken() {
        boolean isFirstRegisterSuccess = registerCommand.execute("takenUser", "P@ssword1!");
        boolean isSecondRegisterSuccess = registerCommand.execute("otherUser", "P@ssword1!");
        User user = userRepository.getByUserName("otherUser").orElseThrow();

        boolean isNameChangeSuccess = changeUserNameCommand.execute(user.getId(), "takenUser");

        assertTrue(isFirstRegisterSuccess);
        assertTrue(isSecondRegisterSuccess);
        assertFalse(isNameChangeSuccess);
    }

    @Test
    void execute_shouldThrow_whenCurrentUserDoesntExist() {
        User user = new User("differentUser", "P@ssword1!");
        // Not registering the user.

        assertThrows(Exception.class,
                () -> changeUserNameCommand.execute(user.getId(), "newUserName"));
    }

    @Test
    void execute_shouldReturnTrueAndChangeUserName_whenValidData() {
        registerCommand.execute("oldUserName", "P@ssword1!");
        User user = userRepository.getByUserName("oldUserName").orElseThrow();

        boolean isNameChangeSuccess = changeUserNameCommand.execute(user.getId(), "newUserName");

        User updatedUser = userRepository.getByUserName("newUserName").orElseThrow();
        assertTrue(isNameChangeSuccess);
        assertEquals("newUserName", updatedUser.getUserName());
    }

}
