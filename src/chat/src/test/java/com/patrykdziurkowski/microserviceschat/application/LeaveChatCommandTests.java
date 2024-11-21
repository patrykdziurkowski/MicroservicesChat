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

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.infrastructure.ChatRepositoryImpl;
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
class LeaveChatCommandTests extends ChatDbContainerBase {
    @Autowired
    private LeaveChatCommand memberLeaveCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void memberLeaveCommand_shouldLoad() {
        assertNotNull(memberLeaveCommand);
    }

    @Test
    void execute_whenValidData_returnsTrue() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);

        boolean didSucceed = memberLeaveCommand.execute(ownerId, chat.getId(), "member");

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenMemberNotInChat_returnsFalse() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);

        boolean didSucceed = memberLeaveCommand.execute(UUID.randomUUID(), chat.getId(), "member");

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenChatDoesntExists_returnsFalse() {
        boolean didSucceed = memberLeaveCommand.execute(UUID.randomUUID(), UUID.randomUUID(), "member");

        assertFalse(didSucceed);
    }

}
