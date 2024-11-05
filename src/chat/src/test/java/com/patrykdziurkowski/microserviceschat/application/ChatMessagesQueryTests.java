package com.patrykdziurkowski.microserviceschat.application;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.infrastructure.ChatRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.infrastructure.MessageRepositoryImpl;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;

@SpringBootTest
@Rollback
@Transactional
@ContextConfiguration(classes = ChatApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class ChatMessagesQueryTests {
    @Autowired
    private ChatMessagesQuery chatMessagesQuery;
    @Autowired
    private MessageRepositoryImpl messageRepository;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @SuppressWarnings("resource")
    @Container
    @ServiceConnection
    private static MSSQLServerContainer<?> db = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-CU15-GDR1-ubuntu-22.04")
            .withExposedPorts(1433)
            .waitingFor(Wait.forSuccessfulCommand(
                    "/opt/mssql-tools18/bin/sqlcmd -U sa -S localhost -P examplePassword123 -No -Q 'SELECT 1'"))
            .acceptLicense()
            .withPassword("examplePassword123");

    @Test
    void chatMessagesQuery_shouldLoad() {
        assertNotNull(chatMessagesQuery);
    }

    @Test
    void execute_whenChatDoesntExists_shouldReturnEmpty() {
        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(UUID.randomUUID(), 0, 20);

        assertTrue(returnedMessages.isEmpty());
    }

    @Test
    void execute_whenChatExistsAndMemberJoined_shouldReturnAnnouncementMessage() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chat.join(UUID.randomUUID(), "member");
        chatRepository.save(chat);

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(chat.getId(), 0, 20);

        assertTrue(returnedMessages.isPresent());
        assertEquals(1, returnedMessages.get().size());
    }

    @Test
    void execute_whenChatExistsAndMessagePosted_shouldReturnMessages() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        UserMessage message = new UserMessage(chat.getId(), "text", UUID.randomUUID());
        chatRepository.save(chat);
        messageRepository.save(message);

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(chat.getId(), 0, 20);

        assertTrue(returnedMessages.isPresent());
        assertEquals(1, returnedMessages.get().size());
    }

    @Test
    void execute_whenChatExistsAndMessagesPosted_shouldReturnSpecifiedNumberOfMessages() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);
        for(int i = 0; i < 5; i++) {
            messageRepository.save(new UserMessage(chat.getId(), "text", UUID.randomUUID()));
        }

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(chat.getId(), 0, 2);

        assertTrue(returnedMessages.isPresent());
        assertEquals(2, returnedMessages.get().size());
    }

    @Test
    void execute_whenChatExistsAndMessagesPosted_shouldReturnOnlyRemainingMessages() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);
        for(int i = 0; i < 5; i++) {
            messageRepository.save(new UserMessage(chat.getId(), "text", UUID.randomUUID()));
        }

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(chat.getId(), 2, 20);

        assertTrue(returnedMessages.isPresent());
        assertEquals(3, returnedMessages.get().size());
    }

    @Test
    void execute_whenChatExistsAndMessagesPosted_shouldntReturnAnyMessages() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "chat", false);
        chatRepository.save(chat);
        for(int i = 0; i < 5; i++) {
            messageRepository.save(new UserMessage(chat.getId(), "text", UUID.randomUUID()));
        }

        Optional<List<UserMessage>> returnedMessages = chatMessagesQuery.execute(chat.getId(), 20, 20);

        assertTrue(returnedMessages.isEmpty());
    }

}

