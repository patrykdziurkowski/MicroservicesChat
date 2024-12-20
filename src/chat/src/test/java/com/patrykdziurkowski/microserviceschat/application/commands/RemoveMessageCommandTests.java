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
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.infrastructure.repositories.ChatRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.infrastructure.repositories.MessageRepositoryImpl;
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
class RemoveMessageCommandTests extends ChatDbContainerBase {
    @Autowired
    private RemoveMessageCommand messageRemoveCommand;
    @Autowired
    private MessageRepositoryImpl messageRepository;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void messageRemoveCommand_shouldLoad() {
        assertNotNull(messageRemoveCommand);
    }

    @Test
    void execute_whenValidData_returnsTrue() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        UserMessage message = new UserMessage(chat.getId(), "text", ownerId);
        chatRepository.save(chat);
        messageRepository.save(message);

        boolean didSucceed = messageRemoveCommand.execute(ownerId, message.getId());

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenOwnerOfChatRemovesMembersMessage_returnsTrue() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        UserMessage message = new UserMessage(chat.getId(), "text", memberId);
        chat.join(memberId, "member");
        chatRepository.save(chat);
        messageRepository.save(message);

        boolean didSucceed = messageRemoveCommand.execute(ownerId, message.getId());

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenMemberTriesToRemoveAnotherMembersMessage_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        UserMessage message = new UserMessage(chat.getId(), "text", ownerId);
        chat.join(memberId, "member");
        chatRepository.save(chat);
        messageRepository.save(message);

        boolean didSucceed = messageRemoveCommand.execute(memberId, message.getId());

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenNonMemberTriesToRemoveMessage_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        UserMessage message = new UserMessage(chat.getId(), "text", ownerId);
        chatRepository.save(chat);
        messageRepository.save(message);

        boolean didSucceed = messageRemoveCommand.execute(UUID.randomUUID(), message.getId());

        assertFalse(didSucceed);
    }

}
