package com.patrykdziurkowski.microserviceschat.infrastructure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace; 
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ContextConfiguration;
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
public class UserMessageRepositoryImplTests {
    @Autowired
    private UserMessageRepositoryImpl userMessageRepository;

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
        assertNotNull(userMessageRepository);
    }

    @Test
    void save_shouldSaveMessage_whenMessageDoesntExists() {
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", UUID.randomUUID());

        userMessageRepository.save(msg);

        List<UserMessage> userMessages = userMessageRepository.get();
        assertEquals(1, userMessages.size());
    }

    @Test
    void save_shouldntSaveMessage_whenMessageExists() {
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", UUID.randomUUID());

        userMessageRepository.save(msg);
        userMessageRepository.save(msg);

        List<UserMessage> userMessages = userMessageRepository.get();
        assertEquals(1, userMessages.size());
    }

    @Test
    void get_shouldReturnEmpty_whenNoMessagesInDatabase() {
        List<UserMessage> userMessages = userMessageRepository.get();

        assertTrue(userMessages.isEmpty());
    }

    @Test
    void get_shouldReturnMessage_whenExists() {
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", UUID.randomUUID());
        userMessageRepository.save(msg);
        
        List<UserMessage> userMessages = userMessageRepository.get();

        assertEquals(1,userMessages.size());
    }

    @Test
    void getById_shouldReturnMessage_whenExistsAndValidId() {
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", UUID.randomUUID());
        UUID msgId = msg.getId();
        userMessageRepository.save(msg);
        
        Optional<UserMessage> returnedMsg = userMessageRepository.getById(msgId);

        assertEquals(msg,returnedMsg.get());
    }

    @Test
    void getById_shouldEmpty_whenExistsAndInvalidId() {
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", UUID.randomUUID());
        userMessageRepository.save(msg);
        
        Optional<UserMessage> returnedMsg = userMessageRepository.getById(UUID.randomUUID());

        assertFalse(returnedMsg.isPresent());
    }

    @Test
    void getByOwnerId_shouldReturnMessage_whenExistsAndValidOwner() {
        UUID msgOwnerId = UUID.randomUUID();
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", msgOwnerId);
        userMessageRepository.save(msg);

        UserMessage returnedMsg = userMessageRepository.getByOwnerId(msgOwnerId).get(0);

        assertEquals(msg,returnedMsg);
    }

    @Test
    void getByOwnerId_shouldntReturnMessage_whenExistsAndInvalidOwner() {
        UUID msgOwnerId = UUID.randomUUID();
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", msgOwnerId);
        userMessageRepository.save(msg);

        List<UserMessage> returnedMsgs = userMessageRepository.getByOwnerId(UUID.randomUUID());

        assertFalse(returnedMsgs.size() > 0);
    }

    @Test
    void getByAmount_shouldReturn3Messages_whenExists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        Optional<UUID> lastMessageId = Optional.ofNullable(null);
        for(int i = 0; i < 3; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, lastMessageId, 3);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        assertEquals(3, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturn2Messages_when3Exists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        Optional<UUID> lastMessageId = Optional.ofNullable(null);
        for(int i = 0; i < 3; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, lastMessageId, 2);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        assertEquals(2, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturn2Messages_when4ExistsAndStartedFromSecond() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        Optional<UUID> lastMessageId = Optional.ofNullable(null);

        for(int i = 1; i < 5; i++) {
            msg = new UserMessage(chatRoomId, "msg" + i, UUID.randomUUID(), LocalDateTime.now().withHour(i));
            userMessageRepository.save(msg);
            if(i == 2) {
                lastMessageId = Optional.ofNullable(msg.getId());
            }
        }
        
        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, lastMessageId, 2);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        UserMessage lastMessage = returnedMsgs.get().get(1);
        assertEquals("msg4", lastMessage.getText());
        assertEquals(2, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturn3Messages_whenOnly3Exists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        Optional<UUID> lastMessageId = Optional.ofNullable(null);
        for(int i = 0; i < 3; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, lastMessageId, 20);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        assertEquals(3, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturnOnly3Messages_whenMoreExists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        Optional<UUID> lastMessageId = Optional.ofNullable(null);
        for(int i = 0; i < 5; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, lastMessageId, 3);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        assertEquals(3, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturnEmpty_whenOnlyStartedFromMessageThatDoesntExists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        Optional<UUID> lastMessageId = Optional.ofNullable(UUID.randomUUID());
        for(int i = 0; i < 5; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, lastMessageId, 20);

        assertTrue(returnedMsgs.isEmpty());
    }

}
