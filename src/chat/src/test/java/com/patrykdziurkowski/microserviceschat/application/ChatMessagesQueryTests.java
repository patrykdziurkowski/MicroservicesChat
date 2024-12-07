package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
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
import com.patrykdziurkowski.microserviceschat.infrastructure.ChatRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.infrastructure.MessageRepositoryImpl;
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
class ChatMessagesQueryTests extends ChatDbContainerBase {
    @Autowired
    private ChatMessagesQuery chatMessagesQuery;
    @Autowired
    private MessageRepositoryImpl messageRepository;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void chatMessagesQuery_shouldLoad() {
        assertNotNull(chatMessagesQuery);
    }

    @Test
    void execute_whenChatDoesntExist_shouldReturnEmpty() {
        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                0,
                20);

        assertTrue(returnedMessages.isEmpty());
    }

    @Test
    void execute_whenChatExistsButMemberNotInChat_shouldReturnEmpty() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chat.join(UUID.randomUUID(), "member");
        chatRepository.save(chat);

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(
                UUID.randomUUID(),
                chat.getId(),
                0,
                20);

        assertTrue(returnedMessages.isEmpty());
    }

    @Test
    void execute_whenChatExistsAndMemberJoined_shouldReturnAnnouncementMessage() {
        UUID joinedUserId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chat.join(joinedUserId, "member");
        chatRepository.save(chat);

        Optional<List<UserMessage>> returnedMessagesResult = chatMessagesQuery.execute(
                joinedUserId,
                chat.getId(),
                0,
                20);

        assertTrue(returnedMessagesResult.isPresent());
        List<UserMessage> returnedMessages = returnedMessagesResult.orElseThrow();
        assertTrue(returnedMessages.size() > 0);
        assertEquals(1, returnedMessages.size());
    }

    @Test
    void execute_whenChatExistsAndMessagePosted_shouldReturnMessages() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        UserMessage message = new UserMessage(chat.getId(), "text", ownerId);
        chatRepository.save(chat);
        messageRepository.save(message);

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(
                ownerId,
                chat.getId(),
                0,
                20);

        assertTrue(returnedMessages.orElseThrow().size() > 0);
        assertEquals(1, returnedMessages.orElseThrow().size());
    }

    @Test
    void execute_whenChatExistsAndMessagesPosted_shouldReturnSpecifiedNumberOfMessages() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        for (int i = 0; i < 5; i++) {
            messageRepository.save(new UserMessage(chat.getId(), "text", ownerId));
        }

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(
                ownerId,
                chat.getId(),
                0,
                2);

        assertTrue(returnedMessages.orElseThrow().size() > 0);
        assertEquals(2, returnedMessages.orElseThrow().size());
    }

    @Test
    void execute_whenChatExistsAndMessagesPosted_shouldReturnOnlyRemainingMessages() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        for (int i = 0; i < 5; i++) {
            messageRepository.save(new UserMessage(chat.getId(), "text", ownerId));
        }

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(
                ownerId,
                chat.getId(),
                2,
                20);

        assertTrue(returnedMessages.orElseThrow().size() > 0);
        assertEquals(3, returnedMessages.orElseThrow().size());
    }

    @Test
    void execute_whenOffsetBiggerThanNumberOfMessages_shouldntReturnAnyMessages() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);
        for (int i = 0; i < 5; i++) {
            messageRepository.save(new UserMessage(chat.getId(), "text", ownerId));
        }

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(
                ownerId,
                chat.getId(),
                20,
                20);

        assertTrue(returnedMessages.orElseThrow().isEmpty());
    }

}
