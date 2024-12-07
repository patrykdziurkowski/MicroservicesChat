package com.patrykdziurkowski.microserviceschat.infrastructure.repositories;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;
import com.patrykdziurkowski.microserviceschat.presentation.ChatDbContainerBase;

@DataJpaTest
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FavoriteChatRepositoryImplTests extends ChatDbContainerBase {
    @Autowired
    private FavoriteChatRepositoryImpl favoriteChatRepository;
    @Autowired
    private ChatRepositoryImpl chatRepository;
    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private ObjectMapper objectMapper;

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
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        FavoriteChatRoom favoriteChat = FavoriteChatRoom.set(ownerId, chat).get();
        favoriteChatRepository.save(favoriteChat);

        FavoriteChatRoom returnedChat = favoriteChatRepository
                .getById(favoriteChat.getId())
                .get();

        assertEquals(favoriteChat.getId(), returnedChat.getId());
    }

    @Test
    void getById_shouldReturnEmpty_whenGivenWrongId() {
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
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        FavoriteChatRoom favoriteChat = FavoriteChatRoom.set(ownerId, chat).get();
        favoriteChatRepository.save(favoriteChat);

        List<FavoriteChatRoom> returnedChats = favoriteChatRepository.getByUserId(ownerId);

        assertFalse(returnedChats.isEmpty());
        assertEquals(favoriteChat.getUserId(), returnedChats.get(0).getUserId());
    }

    @Test
    void save_shouldntReturnChat_whenFavoriteIsUnset() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        FavoriteChatRoom favoriteChat = FavoriteChatRoom.set(ownerId, chat).get();
        favoriteChatRepository.save(favoriteChat);

        favoriteChat.unsetFavorite(favoriteChat.getUserId());
        favoriteChatRepository.save(favoriteChat);
        List<FavoriteChatRoom> returnedChats = favoriteChatRepository.getByUserId(favoriteChat.getUserId());

        assertTrue(returnedChats.isEmpty());
    }

    @Test
    void save_shouldReturn1Chat_whenTheSameIsSetAgain() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        FavoriteChatRoom favoriteChat = FavoriteChatRoom.set(ownerId, chat).get();
        favoriteChatRepository.save(favoriteChat);

        favoriteChatRepository.save(favoriteChat);
        favoriteChatRepository.save(favoriteChat);

        List<FavoriteChatRoom> returnedChats = favoriteChatRepository.getByUserId(favoriteChat.getUserId());

        assertFalse(returnedChats.isEmpty());
        assertEquals(1, returnedChats.size());
    }

}
