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
class LeaveChatCommandTests extends ChatDbContainerBase {
    @Autowired
    private LeaveChatCommand memberLeaveCommand;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @MockBean
    private UserApiClient apiClient;

    @Test
    void memberLeaveCommand_shouldLoad() {
        assertNotNull(memberLeaveCommand);
    }

    @Test
    void execute_whenValidData_returnsTrue() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(ownerId)).thenReturn(Optional.of("kickedMember"));

        Optional<ChatRoom> didSucceed = memberLeaveCommand.execute(ownerId, chat.getId());

        assertTrue(didSucceed.isPresent());
    }

    @Test
    void execute_whenMemberNotInChat_returnsFalse() {
        UUID userId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(userId)).thenReturn(Optional.of("kickedMember"));

        Optional<ChatRoom> didSucceed = memberLeaveCommand.execute(userId, chat.getId());

        assertFalse(didSucceed.isPresent());
    }

    @Test
    void execute_whenChatDoesntExist_returnsFalse() {
        Optional<ChatRoom> didSucceed = memberLeaveCommand.execute(UUID.randomUUID(), UUID.randomUUID());

        assertFalse(didSucceed.isPresent());
    }

    @Test
    void execute_whenUserNameEmpty_returnsFalse() {
        UUID userId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);
        when(apiClient.sendUserNameRequest(userId)).thenReturn(Optional.empty());

        Optional<ChatRoom> didSucceed = memberLeaveCommand.execute(userId, chat.getId());

        assertFalse(didSucceed.isPresent());
    }

}
