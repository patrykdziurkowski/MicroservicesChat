package com.patrykdziurkowski.microserviceschat.application.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.patrykdziurkowski.microserviceschat.application.interfaces.UserApiClient;
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
class InviteMemberCommandTests extends ChatDbContainerBase {
    @Autowired
    private InviteMemberCommand memberInvitationCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @MockBean
    private UserApiClient apiClient;

    @Test
    void memberInvitationCommand_shouldLoad() {
        assertNotNull(memberInvitationCommand);
    }

    @Test
    void execute_givenValidData_returnsTrue() {
        UUID ownerId = UUID.randomUUID();
        UUID invitedId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(invitedId)).thenReturn(Optional.of("invitedUser"));

        boolean didSucceed = memberInvitationCommand.execute(ownerId, chat.getId(), invitedId);

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenNonMemberTriesToInvite_returnsFalse() {
        UUID invitedId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(invitedId)).thenReturn(Optional.of("invitedUser"));

        boolean didSucceed = memberInvitationCommand.execute(UUID.randomUUID(), chat.getId(), invitedId);

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenMemberAlreadyInChat_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(ownerId)).thenReturn(Optional.of("invitedUser"));

        boolean didSucceed = memberInvitationCommand.execute(ownerId, chat.getId(), ownerId);

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenUserNameEmpty_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(ownerId)).thenReturn(Optional.empty());

        boolean didSucceed = memberInvitationCommand.execute(ownerId, chat.getId(), ownerId);

        assertFalse(didSucceed);
    }
}
