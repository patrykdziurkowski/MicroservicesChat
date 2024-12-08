package com.patrykdziurkowski.microserviceschat.application.commands;

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
class SetFavoriteCommandTests extends ChatDbContainerBase {
    @Autowired
    private SetFavoriteCommand favoriteSetCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void favoriteSetCommand_shouldLoad() {
        assertNotNull(favoriteSetCommand);
    }

    @Test
    void execute_whenValidData_returnsTrue() {
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "text", false);
        chat.join(memberId, "member");
        chatRepository.save(chat);

        boolean didSucceed = favoriteSetCommand.execute(memberId, chat.getId());

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenMemberNotInChat_returnsFalse() {
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "text", false);
        chatRepository.save(chat);

        boolean didSucceed = favoriteSetCommand.execute(memberId, chat.getId());

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenChatDoesntExists_returnsFalse() {
        UUID memberId = UUID.randomUUID();

        boolean didSucceed = favoriteSetCommand.execute(memberId, UUID.randomUUID());

        assertFalse(didSucceed);
    }
}
