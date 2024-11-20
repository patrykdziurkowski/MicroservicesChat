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
class UnsetFavoriteCommandTests extends ChatDbContainerBase {
    @Autowired
    private UnsetFavoriteCommand favoriteUnsetCommand;
    @Autowired
    private SetFavoriteCommand favoriteSetCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void favoriteUnsetCommand_shouldLoad() {
        assertNotNull(favoriteUnsetCommand);
    }

    @Test
    void execute_whenGivenValidData_returnsTrue() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        favoriteSetCommand.execute(ownerId, chat.getId());

        boolean didSucceed = favoriteUnsetCommand.execute(ownerId, chat.getId());

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenMemberNotInChat_reuturnsFalse() {
        boolean didSucceed = favoriteUnsetCommand.execute(UUID.randomUUID(), UUID.randomUUID());

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenNotFavorite_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);

        boolean didSucceed = favoriteUnsetCommand.execute(ownerId, chat.getId());

        assertFalse(didSucceed);
    }
}
