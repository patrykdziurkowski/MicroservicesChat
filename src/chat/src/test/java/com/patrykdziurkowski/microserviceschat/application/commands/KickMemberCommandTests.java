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
class KickMemberCommandTests extends ChatDbContainerBase {
    @Autowired
    private KickMemberCommand memberRemoveCommand;
    @Autowired
    private InviteMemberCommand memberInvitationCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @MockBean
    private UserApiClient apiClient;

    @Test
    void memberRemoveCommand_shouldLoad() {
        assertNotNull(memberRemoveCommand);
    }

    @Test
    void execute_whenValidData_returnsTrue() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(memberId)).thenReturn(Optional.of("kickedMember"));
        memberInvitationCommand.execute(ownerId, chat.getId(), memberId);

        boolean didSucceed = memberRemoveCommand.execute(ownerId, chat.getId(), memberId);

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenNonOwnerTriesToRemoveMember_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(memberId)).thenReturn(Optional.of("kickedMember"));
        memberInvitationCommand.execute(ownerId, chat.getId(), memberId);

        boolean didSucceed = memberRemoveCommand.execute(memberId, chat.getId(), memberId);

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenOwnerTriesToRemoveThemselves_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);

        boolean didSucceed = memberRemoveCommand.execute(ownerId, chat.getId(), ownerId);

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenNonMemberTriesToRemoveMember_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(memberId)).thenReturn(Optional.of("kickedMember"));
        memberInvitationCommand.execute(ownerId, chat.getId(), memberId);

        boolean didSucceed = memberRemoveCommand.execute(UUID.randomUUID(), chat.getId(), memberId);

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenUserNameEmpty_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(memberId)).thenReturn(Optional.empty());
        memberInvitationCommand.execute(ownerId, chat.getId(), memberId);

        boolean didSucceed = memberRemoveCommand.execute(UUID.randomUUID(), chat.getId(), memberId);

        assertFalse(didSucceed);
    }
}
