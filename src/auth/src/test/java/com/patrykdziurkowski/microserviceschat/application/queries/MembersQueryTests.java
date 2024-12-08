package com.patrykdziurkowski.microserviceschat.application.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.patrykdziurkowski.microserviceschat.infrastructure.UserRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.presentation.AuthApplication;
import com.patrykdziurkowski.microserviceschat.presentation.AuthDbContainerBase;

import io.jsonwebtoken.lang.Collections;
import jakarta.transaction.Transactional;

@SpringBootTest
@Rollback
@Transactional
@ContextConfiguration(classes = AuthApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MembersQueryTests extends AuthDbContainerBase {
    @Autowired
    private MembersQuery membersQuery;
    @Autowired
    private UserRepositoryImpl userRepository;

    @Test
    void membersQuery_shouldLoad() {
        assertNotNull(membersQuery);
    }

    @Test
    void execute_shouldReturnEmptyList_whenEmptyListProvided() {
        List<User> users = membersQuery.execute(Collections.emptyList());

        assertTrue(users.isEmpty());
    }

    @Test
    void execute_shouldReturnUserNames_whenProvidedAListOfIds() {
        User user1 = new User("userName1", "passwordHash");
        User user2 = new User("userName2", "passwordHash");
        User user3 = new User("userName3", "passwordHash");
        List<User> users = List.of(user1, user2, user3);
        userRepository.save(users);

        List<User> returnedUsers = membersQuery.execute(List.of(user1.getId(), user2.getId()));

        assertTrue(returnedUsers.contains(user1));
        assertTrue(returnedUsers.contains(user2));
        assertEquals(2, returnedUsers.size());
    }
}
