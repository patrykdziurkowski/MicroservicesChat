package com.patrykdziurkowski.microserviceschat.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UserTests {
    @Test
    public void constructor_shouldInitializeFields_whenGivenValidData() {
        User user = new User("username", "password");

        assertEquals(user.getUserName(), "username");
    }

    @Test
    public void login_shouldReturnFalse_whenGivenMatchingUserNameButNotPassword() {
        User user = new User("username", "password");

        boolean isLoggedIn = user.login("username", "password123");

        assertFalse(isLoggedIn);
    }

    @Test
    public void login_shouldReturnFalse_whenGivenMatchingPasswordButNotUserName() {
        User user = new User("username", "password");

        boolean isLoggedIn = user.login("username1", "password");

        assertFalse(isLoggedIn);
    }

    @Test
    public void login_shouldReturnFalse_whenGivenNonMatchingUserNamePassword() {
        User user = new User("username", "password");

        boolean isLoggedIn = user.login("username1", "3password");

        assertFalse(isLoggedIn);
    }

    @Test
    public void login_shouldReturnFalse_whenGivenEmptyUserName() {
        User user = new User("username", "password");

        boolean isLoggedIn = user.login("", "3password");

        assertFalse(isLoggedIn);
    }

    @Test
    public void login_shouldReturnFalse_whenGivenEmptyPassword() {
        User user = new User("username", "password");

        boolean isLoggedIn = user.login("username", "");

        assertFalse(isLoggedIn);
    }

    @Test
    public void login_shouldReturnTrue_whenGivenMatchingUserNamePassword() {
        User user = new User("username", "password");

        boolean isLoggedIn = user.login("username", "password");

        assertTrue(isLoggedIn);
    }
}
