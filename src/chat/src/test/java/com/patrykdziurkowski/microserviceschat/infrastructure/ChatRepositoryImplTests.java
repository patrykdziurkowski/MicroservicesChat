package com.patrykdziurkowski.microserviceschat.infrastructure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;

@DataJpaTest
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class ChatRepositoryImplTests {
    @Autowired
    private ChatRepositoryImpl chatRepository;
    @Autowired
    private UserMessageRepositoryImpl messageRepository;
    @Autowired
    private FavoriteChatRepositoryImpl favoriteChatRepository;

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
        assertNotNull(chatRepository);
    }

    @Test
    void getAll_shouldReturnEmpty_whenNoChatRoomInDatabase() {
        List<ChatRoom> chats = chatRepository.get();

        assertTrue(chats.isEmpty());
    }

    @Test
    void getById_shouldReturnChat_whenExists() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        chatRepository.save(chat);
        UUID chatId = chat.getId();

        ChatRoom chatFromDb = chatRepository.getById(chatId).get();

        assertNotNull(chatFromDb);
        assertTrue(chatFromDb.getId().equals(chatId));
    }

    @Test
    void getByMemberId_shouldReturnChat_whenExistsAndUserIsMember() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", true);
        chatRepository.save(chat);
        UUID memberId = UUID.randomUUID();
        chat.join(memberId);

        chatRepository.save(chat);
        List<ChatRoom> chats = chatRepository.getByMemberId(memberId);

        assertTrue(chats.contains(chat));
    }

    @Test
    void getByMemberId_shouldReturnChat_whenExistsAndChatIsPublic() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", true);
        chatRepository.save(chat);
        UUID memberId = UUID.randomUUID();

        chatRepository.save(chat);
        List<ChatRoom> chats = chatRepository.getByMemberId(memberId);

        assertTrue(chats.contains(chat));
    }

    @Test
    void getByMemberId_shouldReturnNull_whenExistsAndChatIsPrivate() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        chatRepository.save(chat);
        UUID memberId = UUID.randomUUID();

        chatRepository.save(chat);
        List<ChatRoom> chats = chatRepository.getByMemberId(memberId);

        assertTrue(chats.contains(chat) == false);
    }

    @Test
    void getByMemberId_shouldReturnChat_whenExistsAndUserIsMemberAndChatIsPrivate() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        chatRepository.save(chat);
        UUID memberId = UUID.randomUUID();
        chat.join(memberId);

        chatRepository.save(chat);
        List<ChatRoom> chats = chatRepository.getByMemberId(memberId);

        assertTrue(chats.contains(chat));
    }

    @Test
    void save_shouldSaveChat_whenNew() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);

        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        assertFalse(chats.isEmpty());
    }

    @Test
    void save_shouldSave2Chat_when2NewChats() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        ChatRoom chat2 = new ChatRoom(UUID.randomUUID(), "Chat", false);

        chatRepository.save(chat);
        chatRepository.save(chat2);

        List<ChatRoom> chats = chatRepository.get();
        assertFalse(chats.isEmpty());
        assertTrue(chats.size() == 2);
    }

    @Test
    void save_shouldSaveChat_whenChatAdded2Times() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);

        chatRepository.save(chat);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        assertFalse(chats.isEmpty());
        assertTrue(chats.size() == 1);
    }

    @Test
    void save_shouldDeleteChat_whenChatDissolved() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", false);
        chatRepository.save(chat);

        chat.dissolve(ownerId);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        assertTrue(chats.isEmpty());
    }

    @Test
    void save_shouldDeleteChatAndMessages_whenChatDissolved() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", false);
        UserMessage userMsg = new UserMessage(chat.getId(), "test", UUID.randomUUID());
        chatRepository.save(chat);
        messageRepository.save(userMsg);

        chat.dissolve(ownerId);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<UserMessage> msgs = messageRepository.getByOwnerId(userMsg.getOwnerId());
        assertTrue(chats.isEmpty());
        assertTrue(msgs.isEmpty());
    }

    @Test
    void save_shouldDeleteChatAndNotMessages_whenChatDissolvedAndMessagesInAnotherChat() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", false);
        UserMessage userMsg = new UserMessage(UUID.randomUUID(), "test", UUID.randomUUID());
        chatRepository.save(chat);
        messageRepository.save(userMsg);

        chat.dissolve(ownerId);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<UserMessage> msgs = messageRepository.getByOwnerId(userMsg.getOwnerId());
        assertTrue(chats.isEmpty());
        assertFalse(msgs.isEmpty());
    }

    @Test
    void save_shouldDeleteChatAndFavoriteChats_whenChatDissolved() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", false);
        FavoriteChatRoom favChat = new FavoriteChatRoom(chat.getId(),ownerId);
        chatRepository.save(chat);
        favoriteChatRepository.save(favChat);

        chat.dissolve(ownerId);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<FavoriteChatRoom> favChats = favoriteChatRepository.getByUserId(ownerId);
        assertTrue(chats.isEmpty());
        assertTrue(favChats.isEmpty());
    }

    @Test
    void save_shouldDeleteChatAndNotFavoriteChats_whenChatDissolvedAndFavoritedAnotherChats() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", false);
        FavoriteChatRoom favChat = new FavoriteChatRoom(UUID.randomUUID(),ownerId);
        chatRepository.save(chat);
        favoriteChatRepository.save(favChat);

        chat.dissolve(ownerId);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<FavoriteChatRoom> favChats = favoriteChatRepository.getByUserId(ownerId);
        assertTrue(chats.isEmpty());
        assertFalse(favChats.isEmpty());
    }


}
