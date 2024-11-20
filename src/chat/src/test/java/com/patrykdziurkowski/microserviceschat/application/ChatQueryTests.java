package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.patrykdziurkowski.microserviceschat.ChatDbContainerBase;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.infrastructure.ChatRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;

@SpringBootTest
@Rollback
@Transactional
@ContextConfiguration(classes = ChatApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ChatQueryTests extends ChatDbContainerBase {
    @Autowired
    private ChatQuery chatQuery;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void chatQuery_shouldLoad() {
        assertNotNull(chatQuery);
    }

    @Test
    void execute_whenChatDoesntExists_shouldReturnEmpty() {
        Optional<ChatRoom> returnedChat = chatQuery.execute(UUID.randomUUID());

        assertTrue(returnedChat.isEmpty());
    }

    @Test
    void execute_whenChatExists_shouldReturnChat() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);

        Optional<ChatRoom> returnedChat = chatQuery.execute(chat.getId());

        assertTrue(returnedChat.isPresent());
    }

}