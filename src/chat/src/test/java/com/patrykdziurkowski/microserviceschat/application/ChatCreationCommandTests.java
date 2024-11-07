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
@Testcontainers
class ChatCreationCommandTests {
    @Autowired
    private ChatCreationCommand chatCreationCommand;
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
        assertNotNull(chatCreationCommand);
    }

    @Test
    void execute_whenValidData_shouldAddChat() {
        chatCreationCommand.execute(UUID.randomUUID(), "chat", false, Optional.ofNullable("password"));

        List<ChatRoom> chats = chatRepository.get();
        ChatRoom chat = chats.get(0);
        assertEquals(1, chats.size());
        assertTrue(chat.getName().equals("chat"));
        assertFalse(chat.getIsPublic());
        assertFalse(chat.getPasswordHash().get().equals("password"));
    }

    @Test
    void execute_whenDiffrentValidData_shouldAddChat() {
        chatCreationCommand.execute(UUID.randomUUID(), "chat", true, Optional.empty());

        List<ChatRoom> chats = chatRepository.get();
        ChatRoom chat = chats.get(0);
        assertEquals(1, chats.size());
        assertTrue(chat.getName().equals("chat"));
        assertTrue(chat.getIsPublic());
        assertTrue(chat.getPasswordHash().isEmpty());
    }

    
}
