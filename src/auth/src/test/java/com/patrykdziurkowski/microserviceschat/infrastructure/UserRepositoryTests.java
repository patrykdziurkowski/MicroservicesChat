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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.patrykdziurkowski.microserviceschat.domain.User;
import com.patrykdziurkowski.microserviceschat.presentation.AuthApplication;

@DataJpaTest
@ContextConfiguration(classes = AuthApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
public class UserRepositoryTests {
    @Autowired
    private UserRepository userRepository;

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
    public void repository_shouldLoad() {
        assertNotNull(userRepository);
    }

    @Test
    public void get_shouldReturnEmpty_whenNoUsers() {
        List<User> users = userRepository.get();

        assertEquals(0, users.size());
    }

    @Test
    public void get_shouldReturnUser_whenSingleUserExists() {
        User user = new User("userName", "password");
        userRepository.save(user);

        List<User> users = userRepository.get();

        assertEquals(1, users.size());
    }

    @Test
    public void get_shouldReturnThreeUsers_whenGivenThreeUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("userName1", "password"));
        users.add(new User("userName2", "password"));
        users.add(new User("userName3", "password"));
        userRepository.save(users);

        List<User> usersInDatabase = userRepository.get();

        assertEquals(3, usersInDatabase.size());
    }

    @Test
    public void getById_shouldReturnEmpty_whenNoMatchingUserExists() {
        User user = new User("userName123", "password");
        // Not saving the user: userRepository.save(user);

        Optional<User> result = userRepository.getById(user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    public void getById_shouldReturnExistingUser_whenMatchingUserExists() {
        User user = new User("userName123", "password");
        userRepository.save(user);

        Optional<User> result = userRepository.getById(user.getId());
        User selectedUser = result.orElseThrow();

        assertTrue(result.isPresent());
        assertEquals(user.getId(), selectedUser.getId());
        assertEquals("userName123", selectedUser.getUserName());
    }

    @Test
    public void getByUserName_shouldReturnEmpty_whenNoUserWithMatchingName() {
        Optional<User> result = userRepository.getByUserName("nonExistantName");

        assertTrue(result.isEmpty());
    }

    @Test
    public void getByUserName_shouldReturnUserWithMatchingName_ifExists() {
        User user = new User("existingUser", "password1");
        userRepository.save(user);

        Optional<User> result = userRepository.getByUserName("existingUser");

        assertTrue(result.isPresent());
        assertTrue(user.login(user.getUserName(), "password1"));
    }

    @Test
    public void save_shouldSaveNewEntity_whenNewUserSaved() {
        User user = new User("userName123", "p@ssword");

        userRepository.save(user);

        User createdUser = userRepository.getById(user.getId()).get();
        assertEquals("userName123", createdUser.getUserName());
        assertTrue(createdUser.login(user.getUserName(), "p@ssword"));
    }

    @Test
    public void save_shouldThrow_whenDuplicateNames() {
        User user1 = new User("userName123", "p@ssword");
        User user2 = new User("userName123", "p@ssword2");

        userRepository.save(user1);
        assertThrows(ConstraintViolationException.class, () -> userRepository.save(user2));
    }

    @Test
    public void save_shouldNotDuplicateUser_whenSavedTwice() {
        User user = new User("userName123", "p@ssword");
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user);

        userRepository.save(users);

        List<User> createdUsers = userRepository.get();
        assertEquals(1, createdUsers.size());
    }

    @Test
    public void save_shouldUpdateUser_whenUserNameChanged() {
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
    public void save_shouldUpdateMultipleUsers_whenUserNamesChanged() {
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
