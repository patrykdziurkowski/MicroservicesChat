package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class JoinChatCommandTests {
    @Autowired
    private JoinChatCommand memberJoinCommand;
    @Autowired
    private CreateChatCommand chatCreationCommand;
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
    void memberInvitationCommand_shouldLoad() {
        assertNotNull(memberJoinCommand);
    }

    @Test
    void execute_givenValidData_returnsTrue() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        chatRepository.save(chat);
        
        boolean didSucceed  = memberJoinCommand.execute(UUID.randomUUID(), chat.getId(), "member", null);

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenCorrectPasswordProvided_returnsTrue() {
        chatCreationCommand.execute(UUID.randomUUID(), "chat", false, Optional.ofNullable("password1"));
        ChatRoom chat = chatRepository.get().get(0);

        boolean didSucceed  = memberJoinCommand.execute(UUID.randomUUID(), chat.getId(), "member", Optional.ofNullable("password1"));

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenWrongPasswordProvided_returnsFalse() {
        chatCreationCommand.execute(UUID.randomUUID(), "chat", false, Optional.ofNullable("password1"));
        ChatRoom chat = chatRepository.get().get(0);

        boolean didSucceed  = memberJoinCommand.execute(UUID.randomUUID(), chat.getId(), "member", Optional.ofNullable("password2"));

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenMemberAlreadyInChat_returnsFalse() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "chat", false);
        chatRepository.save(chat);

        boolean didSucceed = memberJoinCommand.execute(ownerId, chat.getId(), "member", null);

        assertFalse(didSucceed);
    }
}
