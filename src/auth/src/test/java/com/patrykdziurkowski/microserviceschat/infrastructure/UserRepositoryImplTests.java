package com.patrykdziurkowski.microserviceschat.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import com.patrykdziurkowski.microserviceschat.domain.User;
import com.patrykdziurkowski.microserviceschat.presentation.AuthApplication;
import com.patrykdziurkowski.microserviceschat.presentation.AuthDbContainerBase;

@DataJpaTest
@ContextConfiguration(classes = AuthApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryImplTests extends AuthDbContainerBase {
    @Autowired
    private UserRepositoryImpl userRepository;

    @Test
    void repository_shouldLoad() {
        assertNotNull(userRepository);
    }

    @Test
    void get_shouldReturnEmpty_whenNoUsers() {
        List<User> users = userRepository.get();

        assertEquals(0, users.size());
    }

    @Test
    void get_shouldReturnUser_whenSingleUserExists() {
        User user = new User("userName", "password");
        userRepository.save(user);

        List<User> users = userRepository.get();

        assertEquals(1, users.size());
    }

    @Test
    void get_shouldReturnThreeUsers_whenGivenThreeUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("userName1", "password"));
        users.add(new User("userName2", "password"));
        users.add(new User("userName3", "password"));
        userRepository.save(users);

        List<User> usersInDatabase = userRepository.get();

        assertEquals(3, usersInDatabase.size());
    }

    @Test
    void getById_shouldReturnEmpty_whenNoMatchingUserExists() {
        User user = new User("userName123", "password");
        // Not saving the user

        Optional<User> result = userRepository.getById(user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_shouldReturnExistingUser_whenMatchingUserExists() {
        User user = new User("userName123", "password");
        userRepository.save(user);

        Optional<User> result = userRepository.getById(user.getId());
        User selectedUser = result.orElseThrow();

        assertTrue(result.isPresent());
        assertEquals(user.getId(), selectedUser.getId());
        assertEquals("userName123", selectedUser.getUserName());
    }

    @Test
    void getByUserName_shouldReturnEmpty_whenNoUserWithMatchingName() {
        Optional<User> result = userRepository.getByUserName("nonExistantName");

        assertTrue(result.isEmpty());
    }

    @Test
    void getByUserName_shouldReturnUserWithMatchingName_ifExists() {
        User user = new User("existingUser", "password1");
        userRepository.save(user);

        Optional<User> result = userRepository.getByUserName("existingUser");

        assertTrue(result.isPresent());
        assertEquals("existingUser", result.get().getUserName());
    }

    @Test
    void save_shouldSaveNewEntity_whenNewUserSaved() {
        User user = new User("userName123", "p@ssword");

        userRepository.save(user);

        User createdUser = userRepository.getById(user.getId()).get();
        assertEquals("userName123", createdUser.getUserName());
    }

    @Test
    void save_shouldThrow_whenDuplicateNames() {
        User user1 = new User("userName123", "p@ssword");
        User user2 = new User("userName123", "p@ssword2");

        userRepository.save(user1);
        assertThrows(ConstraintViolationException.class, () -> userRepository.save(user2));
    }

    @Test
    void save_shouldNotDuplicateUser_whenSavedTwice() {
        User user = new User("userName123", "p@ssword");
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user);

        userRepository.save(users);

        List<User> createdUsers = userRepository.get();
        assertEquals(1, createdUsers.size());
    }

    @Test
    void save_shouldUpdateUser_whenUserNameChanged() {
        User user = new User("oldUserName", "p@ssword");
        userRepository.save(user);
        Optional<User> saveResult = userRepository.getByUserName("oldUserName");

        user.setUserName("newUserName");
        userRepository.save(user);

        assertTrue(saveResult.isPresent());
        assertTrue(userRepository.getByUserName("newUserName").isPresent());
        assertTrue(userRepository.getByUserName("oldUserName").isEmpty());
    }

    @Test
    void save_shouldUpdateMultipleUsers_whenUserNamesChanged() {
        List<User> users = new ArrayList<>();
        users.add(new User("oldUserName1", "p@ssword"));
        users.add(new User("oldUserName2", "p@ssword"));
        userRepository.save(users);

        users.get(0).setUserName("newUserName1");
        users.get(1).setUserName("newUserName2");
        userRepository.save(users);

        assertTrue(userRepository.getByUserName("newUserName1").isPresent());
        assertTrue(userRepository.getByUserName("oldUserName1").isEmpty());
        assertTrue(userRepository.getByUserName("newUserName2").isPresent());
        assertTrue(userRepository.getByUserName("oldUserName2").isEmpty());
    }
}
