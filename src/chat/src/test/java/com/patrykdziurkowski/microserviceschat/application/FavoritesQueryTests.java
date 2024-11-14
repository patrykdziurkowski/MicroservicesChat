package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

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

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;
import com.patrykdziurkowski.microserviceschat.infrastructure.ChatRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.infrastructure.FavoriteChatRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;

@SpringBootTest
@Rollback
@Transactional
@ContextConfiguration(classes = ChatApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class FavoritesQueryTests {
    @Autowired
    private FavoritesQuery favoritesQuery;
    @Autowired
    private FavoriteChatRepositoryImpl favoriteChatRepository;
    @Autowired
    private ChatRepositoryImpl chatRepository;

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
    void favoritesQuery_shouldLoad() {
        assertNotNull(favoritesQuery);
    }

    @Test
    void execute_whenUserDoesntHaveFavoriteChat_shouldReturnEmpty() {
        List<FavoriteChatRoom> returnedChats = favoritesQuery.execute(UUID.randomUUID());

        assertTrue(returnedChats.isEmpty());
    }

    @Test
    void execute_whenUserHaveFavoriteChat_shouldReturnFavoriteChat() {
        UUID userId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(userId, "chat", false);
        chatRepository.save(chat);
        favoriteChatRepository.save(FavoriteChatRoom.set(userId, chat).get());

        List<FavoriteChatRoom> returnedChats = favoritesQuery.execute(userId);

        assertTrue(returnedChats.size() > 0);
    }

    @Test
    void execute_whenUserHave2FavoriteChats_shouldReturnChats() {
        UUID userId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(userId, "chat", false);
        ChatRoom anotherChat = new ChatRoom(userId, "chat", false);
        chatRepository.save(chat);
        favoriteChatRepository.save(FavoriteChatRoom.set(userId, chat).get());
        favoriteChatRepository.save(FavoriteChatRoom.set(userId, anotherChat).get());

        List<FavoriteChatRoom> returnedChats = favoritesQuery.execute(userId);

        assertTrue(returnedChats.size() > 0);
        assertEquals(2, returnedChats.size());
    }
}