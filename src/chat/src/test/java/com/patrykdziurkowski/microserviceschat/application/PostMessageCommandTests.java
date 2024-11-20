package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class PostMessageCommandTests extends ChatDbContainerBase {
    @Autowired
    private PostMessageCommand messagePostCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void messagePostCommand_shouldLoad() {
        assertNotNull(messagePostCommand);
    }

    @Test
    void execute_whenProvidedValidData_shouldReturnTrue() {
        UUID userId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(userId, "chat", false);
        chatRepository.save(chat);

        boolean didSucceed = messagePostCommand.execute(chat.getId(), "text", userId);

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenUserNotInChat_shouldReturnFalse() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);

        boolean didSucceed = messagePostCommand.execute(chat.getId(), "text", UUID.randomUUID());

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenChatDoesntExists_shouldReturnFalse() {
        UUID userId = UUID.randomUUID();

        boolean didSucceed = messagePostCommand.execute(UUID.randomUUID(), "text", userId);

        assertFalse(didSucceed);
    }

}
