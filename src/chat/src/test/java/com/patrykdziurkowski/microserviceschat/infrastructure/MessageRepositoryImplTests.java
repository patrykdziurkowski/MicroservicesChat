package com.patrykdziurkowski.microserviceschat.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;

@DataJpaTest
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
public class MessageRepositoryImplTests {
    @Autowired
    private MessageRepositoryImpl messageRepository;
    @MockBean
    private RestTemplate restTemplate;

    @SuppressWarnings("resource")
    @Container
    @ServiceConnection
    private static MSSQLServerContainer<?> db = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-CU15-GDR1-ubuntu-22.04")
            .withExposedPorts(1433)
            .waitingFor(Wait.forSuccessfulCommand(
                    "/opt/mssql-tools18/bin/sqlcmd -U sa -S localhost -P examplePassword123 -No -Q 'SELECT 1'"))
            .acceptLicense()
            .withPassword("P@ssw0rd");

    @Test
    void repository_shouldLoad() {
        assertNotNull(messageRepository);
    }

    @Test
    void save_shouldSaveMessage_whenMessageDoesntExists() {
        UUID chatId = UUID.randomUUID();
        UserMessage message = new UserMessage(chatId, "text", UUID.randomUUID());

        messageRepository.save(message);

        List<UserMessage> userMessages = messageRepository.getByAmount(chatId, 0, 20);
        assertEquals(1, userMessages.size());
    }

    @Test
    void save_shouldntSaveMessage_whenMessageExists() {
        UUID chatId = UUID.randomUUID();
        UserMessage message = new UserMessage(chatId, "text", UUID.randomUUID());

        messageRepository.save(message);
        messageRepository.save(message);

        List<UserMessage> userMessages = messageRepository.getByAmount(chatId, 0, 20);
        assertEquals(1, userMessages.size());
    }

    @Test
    void getByOwnerId_shouldReturnEmpty_whenNoMessagesInDatabase() {
        List<UserMessage> userMessages = messageRepository.getByAmount(UUID.randomUUID(), 0, 20);

        assertTrue(userMessages.isEmpty());
    }

    @Test
    void getById_shouldReturnMessage_whenExistsAndValidId() {
        UserMessage message = new UserMessage(UUID.randomUUID(), "text", UUID.randomUUID());
        UUID messageId = message.getId();
        messageRepository.save(message);

        Optional<UserMessage> returnedMsg = messageRepository.getById(messageId);

        assertEquals(message, returnedMsg.get());
    }

    @Test
    void getById_shouldEmpty_whenExistsAndInvalidId() {
        UserMessage message = new UserMessage(UUID.randomUUID(), "text", UUID.randomUUID());
        messageRepository.save(message);

        Optional<UserMessage> returnedMessage = messageRepository.getById(UUID.randomUUID());

        assertFalse(returnedMessage.isPresent());
    }

    @Test
    void getByAmount_shouldReturn3Messages_whenExists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage message;
        for (int i = 0; i < 3; i++) {
            message = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            messageRepository.save(message);
        }

        List<UserMessage> returnedMessages = messageRepository
                .getByAmount(chatRoomId, 0, 3);

        int numberOfReturnedMsgs = returnedMessages.size();
        assertEquals(3, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturn2Messages_when3Exists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage message;
        for (int i = 0; i < 3; i++) {
            message = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            messageRepository.save(message);
        }

        List<UserMessage> returnedMessages = messageRepository
                .getByAmount(chatRoomId, 0, 2);

        int numberOfReturnedMsgs = returnedMessages.size();
        assertEquals(2, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturn2Messages_when4ExistsAndStartedFromSecond() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage message;

        for (int i = 1; i < 5; i++) {
            message = new UserMessage(chatRoomId, "msg" + i, UUID.randomUUID(), LocalDateTime.now().withHour(i));
            messageRepository.save(message);
        }

        List<UserMessage> returnedMessages = messageRepository
                .getByAmount(chatRoomId, 2, 2);

        int numberOfReturnedMsgs = returnedMessages.size();
        UserMessage lastMessage = returnedMessages.get(1);
        assertEquals("msg4", lastMessage.getText());
        assertEquals(2, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturn3Messages_whenOnly3Exists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage message;
        for (int i = 0; i < 3; i++) {
            message = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            messageRepository.save(message);
        }

        List<UserMessage> returnedMessages = messageRepository
                .getByAmount(chatRoomId, 0, 20);

        int numberOfReturnedMsgs = returnedMessages.size();
        assertEquals(3, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturnOnly3Messages_whenMoreExists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage message;
        for (int i = 0; i < 5; i++) {
            message = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            messageRepository.save(message);
        }

        List<UserMessage> returnedMessages = messageRepository
                .getByAmount(chatRoomId, 0, 3);

        int numberOfReturnedMsgs = returnedMessages.size();
        assertEquals(3, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturnEmpty_whenOnlyStartedFromMessageThatDoesntExists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage message;
        for (int i = 0; i < 5; i++) {
            message = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            messageRepository.save(message);
        }

        List<UserMessage> returnedMessages = messageRepository
                .getByAmount(chatRoomId, 39, 20);

        assertTrue(returnedMessages.isEmpty());
    }

}
