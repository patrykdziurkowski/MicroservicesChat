package com.patrykdziurkowski.microserviceschat.infrastructure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import jakarta.transaction.Transactional;

@DataJpaTest
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
public class UserMessageRepositoryTests {
    @Autowired
    private UserMessageRepository userMessageRepository;

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

        List<UserMessage> userMessages = userMessageRepository.get().get();
        assertEquals(1, userMessages.size());
    }

    @Test
    void save_shouldntSaveMessage_whenMessageExists() {
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", UUID.randomUUID());

        userMessageRepository.save(msg);
        userMessageRepository.save(msg);

        List<UserMessage> userMessages = userMessageRepository.get().get();
        assertEquals(1, userMessages.size());
    }

    @Test
    void get_shouldReturnEmpty_whenNoMessagesInDatabase() {
        List<UserMessage> userMessages = userMessageRepository.get().get();

        assertTrue(userMessages.isEmpty());
    }

    @Test
    void get_shouldReturnMessage_whenExists() {
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", UUID.randomUUID());
        userMessageRepository.save(msg);
        
        List<UserMessage> userMessages = userMessageRepository.get().get();

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

        UserMessage returnedMsg = userMessageRepository.getByOwnerId(msgOwnerId).get().get(0);

        assertEquals(msg,returnedMsg);
    }

    @Test
    void getByOwnerId_shouldntReturnMessage_whenExistsAndInvalidOwner() {
        UUID msgOwnerId = UUID.randomUUID();
        UserMessage msg = new UserMessage(UUID.randomUUID(), "text", msgOwnerId);
        userMessageRepository.save(msg);

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository.getByOwnerId(UUID.randomUUID());

        assertFalse(returnedMsgs.get().size() > 0);
    }

    @Test
    void getByAmount_shouldReturn3Messages_whenExists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        for(int i = 0; i < 3; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, 0, 3);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        assertEquals(3, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturn2Messages_when3Exists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        for(int i = 0; i < 3; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, 0, 2);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        assertEquals(2, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturn2Messages_when3ExistsAndStartedFromSecond() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        String text;
        for(int i = 1; i < 4; i++) {
            text = "msg number: " + i;
            msg = new UserMessage(chatRoomId, text, UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, 1, 2);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        UserMessage lastMessage = returnedMsgs.get().get(1);
        assertEquals("msg number: 3", lastMessage.getText());
        assertEquals(2, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturn3Messages_whenOnly3Exists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        for(int i = 0; i < 3; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, 0, 20);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        assertEquals(3, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturnOnly3Messages_whenMoreExists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        for(int i = 0; i < 5; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, 0, 3);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        assertEquals(3, numberOfReturnedMsgs);
    }

    @Test
    void getByAmount_shouldReturnEmpty_whenOnlyStartedFromMessageThatDoesntExists() {
        UUID chatRoomId = UUID.randomUUID();
        UserMessage msg;
        for(int i = 0; i < 5; i++) {
            msg = new UserMessage(chatRoomId, "test", UUID.randomUUID());
            userMessageRepository.save(msg);
        }

        Optional<List<UserMessage>> returnedMsgs = userMessageRepository
            .getByAmount(chatRoomId, 8, 20);

        int numberOfReturnedMsgs = returnedMsgs.get().size();
        assertEquals(0, numberOfReturnedMsgs);
    }

}
