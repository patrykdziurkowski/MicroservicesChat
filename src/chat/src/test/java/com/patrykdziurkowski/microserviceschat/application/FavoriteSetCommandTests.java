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
public class FavoriteSetCommandTests {
    @Autowired
    private FavoriteSetCommand favoriteCreationCommand;
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
    void favoriteCreationCommand_shouldLoad() {
        assertNotNull(favoriteCreationCommand);
    }

    @Test
    void execute_whenValidData_returnsTrue() {
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "text", false);
        chat.join(memberId, "member");
        chatRepository.save(chat);

        boolean didSucceed = favoriteCreationCommand.execute(memberId, chat.getId());

        assertTrue(didSucceed);
    }

    @Test
    void execute_whenMemberNotInChat_returnsFalse() {
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "text", false);
        chatRepository.save(chat);

        boolean didSucceed = favoriteCreationCommand.execute(memberId, chat.getId());

        assertFalse(didSucceed);
    }

    @Test
    void execute_whenChatDoesntExists_returnsFalse() {
        UUID memberId = UUID.randomUUID();

        boolean didSucceed = favoriteCreationCommand.execute(memberId, UUID.randomUUID());

        assertFalse(didSucceed);
    }
}
