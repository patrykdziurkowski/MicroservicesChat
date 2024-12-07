package com.patrykdziurkowski.microserviceschat.application;

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
class JoinChatCommandTests extends ChatDbContainerBase {
    @Autowired
    private JoinChatCommand memberJoinCommand;
    @Autowired
    private CreateChatCommand chatCreationCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @MockBean
    private UserApiClient apiClient;

    @Test
    void memberInvitationCommand_shouldLoad() {
        assertNotNull(memberJoinCommand);
    }

    @Test
    void execute_givenValidData_returnsTrue() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        chatRepository.save(chat);
        UUID userId = UUID.randomUUID();
        when(apiClient.sendUserNameRequest(userId)).thenReturn(Optional.of("userName"));

        Optional<ChatRoom> joinedChatResult = memberJoinCommand.execute(userId, chat.getId(), null);

        assertTrue(joinedChatResult.isPresent());
    }

    @Test
    void execute_whenCorrectPasswordProvided_returnsTrue() {
        chatCreationCommand.execute(UUID.randomUUID(), "chat", false, Optional.ofNullable("password1"));
        ChatRoom chat = chatRepository.get().get(0);
        UUID userId = UUID.randomUUID();
        when(apiClient.sendUserNameRequest(userId)).thenReturn(Optional.of("userName"));

        Optional<ChatRoom> joinedChatResult = memberJoinCommand.execute(userId, chat.getId(),
                Optional.ofNullable("password1"));

        assertTrue(joinedChatResult.isPresent());
    }

    @Test
    void execute_whenWrongPasswordProvided_returnsFalse() {
        chatCreationCommand.execute(UUID.randomUUID(), "chat", false, Optional.ofNullable("password1"));
        ChatRoom chat = chatRepository.get().get(0);
        UUID userId = UUID.randomUUID();
        when(apiClient.sendUserNameRequest(userId)).thenReturn(Optional.of("userName"));

        Optional<ChatRoom> joinedChatResult = memberJoinCommand.execute(userId, chat.getId(),
                Optional.ofNullable("password2"));

        assertFalse(joinedChatResult.isPresent());
    }

    @Test
    void execute_whenMemberAlreadyInChat_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(ownerId)).thenReturn(Optional.of("userName"));

        Optional<ChatRoom> joinedChatResult = memberJoinCommand.execute(ownerId, chat.getId(), null);

        assertFalse(joinedChatResult.isPresent());
    }

    @Test
    void execute_whenUserNameEmpty_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(ownerId)).thenReturn(Optional.empty());

        Optional<ChatRoom> joinedChatResult = memberJoinCommand.execute(ownerId, chat.getId(), null);

        assertFalse(joinedChatResult.isPresent());
    }
}
