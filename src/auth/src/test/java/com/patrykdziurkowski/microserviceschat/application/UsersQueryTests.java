package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.patrykdziurkowski.microserviceschat.domain.User;
import com.patrykdziurkowski.microserviceschat.presentation.AuthApplication;
import com.patrykdziurkowski.microserviceschat.presentation.AuthDbContainerBase;

import jakarta.transaction.Transactional;

@SpringBootTest
@Rollback
@Transactional
@ContextConfiguration(classes = AuthApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UsersQueryTests extends AuthDbContainerBase {
    @Autowired
    private UsersQuery usersQuery;
    @Autowired
    private UserRepository userRepository;

    @Test
    void changeUserNameCommand_shouldLoad() {
        assertNotNull(usersQuery);
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoUsers() {
        List<User> returnedUsers = userRepository.getByNumber(20, 0);

        assertTrue(returnedUsers.isEmpty());
    }

    @Test
    void execute_shouldReturnListOfOneUser_whenOneUser() {
        userRepository.save(List.of(new User("userName", "passwordHash")));

        List<User> returnedUsers = userRepository.getByNumber(20, 0);

        assertEquals(1, returnedUsers.size());
    }

    @Test
    void execute_shouldReturnThreeUsers_whenThreePresent() {
        List<User> users = new ArrayList<>();
        users.add(new User("userName1", "passwordHash"));
        users.add(new User("userName2", "passwordHash"));
        users.add(new User("userName3", "passwordHash"));
        userRepository.save(users);

        List<User> returnedUsers = userRepository.getByNumber(20, 0);

        assertEquals(3, returnedUsers.size());
    }

    @Test
    void execute_shouldReturnFourUsers_when24PresentAndOffsetIs20() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 24; ++i) {
            users.add(new User("userName" + i, "passwordHash"));
        }
        userRepository.save(users);

        List<User> returnedUsers = userRepository.getByNumber(20, 20);

        assertEquals(4, returnedUsers.size());
    }

    @Test
    void execute_shouldReturnTwoUsers_whenFiltered() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 20; ++i) {
            users.add(new User("userName" + i, "passwordHash"));
        }
        userRepository.save(users);

        List<User> returnedUsers = userRepository.getByNumber(20, 0, "9");

        assertEquals(2, returnedUsers.size());
    }
}
