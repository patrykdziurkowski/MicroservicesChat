package com.patrykdziurkowski.microserviceschat.application.commands;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.infrastructure.repositories.ChatRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;
import com.patrykdziurkowski.microserviceschat.presentation.ChatDbContainerBase;

@SpringBootTest
@Rollback
@Transactional
@ContextConfiguration(classes = ChatApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CreateChatCommandTests extends ChatDbContainerBase {
    @Autowired
    private CreateChatCommand chatCreationCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void chatMessagesQuery_shouldLoad() {
        assertNotNull(chatCreationCommand);
    }

    @Test
    void execute_whenWithPassword_shouldAddChat() {
        chatCreationCommand.execute(UUID.randomUUID(), "chat", false, Optional.ofNullable("password"));

        List<ChatRoom> chats = chatRepository.get();
        ChatRoom chat = chats.get(0);
        assertEquals(1, chats.size());
        assertEquals("chat", chat.getName());
        assertFalse(chat.getIsPublic());
        assertNotEquals("password", chat.getPasswordHash().get());
    }

    @Test
    void execute_whenNoPassword_shouldAddChat() {
        chatCreationCommand.execute(UUID.randomUUID(), "chat", true, Optional.empty());

        List<ChatRoom> chats = chatRepository.get();
        ChatRoom chat = chats.get(0);
        assertEquals(1, chats.size());
        assertEquals("chat", chat.getName());
        assertTrue(chat.getIsPublic());
        assertTrue(chat.getPasswordHash().isEmpty());
    }

}
