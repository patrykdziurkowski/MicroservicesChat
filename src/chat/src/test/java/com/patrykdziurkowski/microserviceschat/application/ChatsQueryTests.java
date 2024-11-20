package com.patrykdziurkowski.microserviceschat.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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
import com.patrykdziurkowski.microserviceschat.infrastructure.ChatRepositoryImpl;
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
class ChatsQueryTests extends ChatDbContainerBase {
    @Autowired
    private ChatsQuery chatsQuery;
    @Autowired
    private ChatRepositoryImpl chatRepository;

    @Test
    void chatsQuery_shouldLoad() {
        assertNotNull(chatsQuery);
    }

    @Test
    void execute_whenUserIsntMemberOfAnyChat_shouldReturnEmpty() {
        List<ChatRoom> returnedChats = chatsQuery.execute(UUID.randomUUID());

        assertTrue(returnedChats.isEmpty());
    }

    @Test
    void execute_whenUserIsMemberOfChat_shouldReturnChat() {
        UUID userId = UUID.randomUUID();
        chatRepository.save(new ChatRoom(userId, "chat", false));

        List<ChatRoom> returnedChats = chatsQuery.execute(userId);

        assertTrue(returnedChats.size() > 0);
    }

    @Test
    void execute_whenUserIsMemberOfChats_shouldReturnChats() {
        UUID userId = UUID.randomUUID();
        chatRepository.save(new ChatRoom(userId, "chat", false));
        chatRepository.save(new ChatRoom(userId, "another chat", false));

        List<ChatRoom> returnedChats = chatsQuery.execute(userId);

        assertTrue(returnedChats.size() > 0);
        assertEquals(2, returnedChats.size());
    }
}