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
class InviteMemberCommandTests extends ChatDbContainerBase {
    @Autowired
    private InviteMemberCommand memberInvitationCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void memberInvitationCommand_shouldLoad() {
        assertNotNull(memberInvitationCommand);
    }

    @Test
    void execute_givenValidData_returnsTrue() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", false);
        chatRepository.save(chat);

        boolean didSucceed = memberInvitationCommand.execute(ownerId, chat.getId(), UUID.randomUUID(), "member");

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenNonMemberTriesToInvite_returnsFalse() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);

        boolean didSucceed = memberInvitationCommand.execute(UUID.randomUUID(), chat.getId(), UUID.randomUUID(),
                "member");

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenMemberAlreadyInChat_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);

        boolean didSucceed = memberInvitationCommand.execute(ownerId, chat.getId(), ownerId, "member");

        assertFalse(didSucceed);
    }
}
