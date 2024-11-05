package com.patrykdziurkowski.microserviceschat.infrastructure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.Hibernate;
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

import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;

@DataJpaTest
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
public class FavoriteChatRepositoryImplTests {
    @Autowired
    private FavoriteChatRepositoryImpl favoriteChatRepository;


    @SuppressWarnings("resource")
    @Container
    @ServiceConnection
    private static MSSQLServerContainer<?> db = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-CU15-GDR1-ubuntu-22.04")
            .withExposedPorts(1433)
            .waitingFor(Wait.forSuccessfulCommand(
                    "/opt/mssql-tools18/bin/sqlcmd -U sa -S localhost -P examplePassword123 -No -Q 'SELECT 1'"))
            .acceptLicense()
            .withPassword("P@ssw0rd");

    @Test
    void repository_shouldLoad() {
        assertNotNull(favoriteChatRepository);
    }

    @Test
    void getByUserId_shouldReturnEmpty_whenNoChatRoomInDatabase() {
        List<FavoriteChatRoom> chats = favoriteChatRepository.getByUserId(UUID.randomUUID());

        assertTrue(chats.isEmpty());
    }

    @Test
    void getById_shouldReturnChat_whenFavoriteChatRoomInDatabase() {
        FavoriteChatRoom chat = new FavoriteChatRoom(UUID.randomUUID(), UUID.randomUUID());
        favoriteChatRepository.save(chat);

        FavoriteChatRoom returnedChat = favoriteChatRepository
            .getById(chat.getId())
            .get();

        assertTrue(chat.getId().equals(returnedChat.getId()));
    }

    @Test
    void getById_shouldReturnEmpty_whenGivenWrongId() {
        FavoriteChatRoom chat = new FavoriteChatRoom(UUID.randomUUID(), UUID.randomUUID());
        favoriteChatRepository.save(chat);

        Optional<FavoriteChatRoom> returnedChat = favoriteChatRepository
            .getById(UUID.randomUUID());

        assertTrue(returnedChat.isEmpty());
    }

    @Test
    void getByUserId_shouldReturnEmpty_whenUserDoesntHaveFavorite() {
        List<FavoriteChatRoom> returnedChats = favoriteChatRepository.getByUserId(UUID.randomUUID());

        assertTrue(returnedChats.isEmpty());
    }

    @Test
    void getByUserId_shouldReturnChat_whenUserDoesHaveFavorite() {
        FavoriteChatRoom chat = new FavoriteChatRoom(UUID.randomUUID(), UUID.randomUUID());
        favoriteChatRepository.save(chat);

        List<FavoriteChatRoom> returnedChats = favoriteChatRepository.getByUserId(chat.getUserId());

        assertTrue(returnedChats.isEmpty() == false);
        assertTrue(returnedChats
            .get(0)
            .getUserId()
            .equals(chat.getUserId()));
    }

    @Test
    void save_shouldntReturnChat_whenFavoriteIsUnset() {
        FavoriteChatRoom chat = new FavoriteChatRoom(UUID.randomUUID(), UUID.randomUUID());
        favoriteChatRepository.save(chat);

        chat.unsetFavorite(chat.getUserId());
        favoriteChatRepository.save(chat);
        List<FavoriteChatRoom> returnedChats = favoriteChatRepository.getByUserId(chat.getUserId());

        assertTrue(returnedChats.isEmpty());
    }

    @Test
    void save_shouldReturn1Chat_whenTheSameIsSetAgain() {
        FavoriteChatRoom chat = new FavoriteChatRoom(UUID.randomUUID(), UUID.randomUUID());

        favoriteChatRepository.save(chat);
        favoriteChatRepository.save(chat);

        List<FavoriteChatRoom> returnedChats = favoriteChatRepository.getByUserId(chat.getUserId());

        assertTrue(returnedChats.isEmpty() == false);
        assertTrue(returnedChats.size() == 1);
    }

}
