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
    private MessageRepositoryImpl messageRepository;
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
        chat.join(memberId, "username");

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
        chat.join(memberId, "username");

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
    void save_shouldSaveOnly1Chat_whenSameChatAdded2Times() {
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
    void save_shouldMakeAnnouncement_whenMemberJoined() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", true);
        chat.join(UUID.randomUUID(), "username");

        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<UserMessage> messages = messageRepository.getByAmount(chat.getId(), 0 , 20);
        assertTrue(chats.size() == 1);
        assertTrue(messages.size() == 1);
        assertTrue(messages.get(0).getText().equals("username joined!"));
    }

    @Test
    void save_shouldMakeAnnouncement_whenMemberInvitedAndJoined() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", true);
        chat.inviteMember(UUID.randomUUID(), "username", ownerId);

        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<UserMessage> messages = messageRepository.getByAmount(chat.getId(), 0 , 20);
        assertTrue(chats.size() == 1);
        assertTrue(messages.size() == 1);
        assertTrue(messages.get(0).getText().equals("username joined through invite!"));
    }

    @Test
    void save_shouldMakeAnnouncement_whenMemberRemoved() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", true);
        chat.inviteMember(memberId, "username", ownerId);
        chat.removeMember(memberId, "username", ownerId);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<UserMessage> messages = messageRepository.getByAmount(chat.getId(), 0 , 20);
        assertTrue(chats.size() == 1);
        assertTrue(messages.size() == 2);
        assertTrue((messages.get(0).getText().equals("username got removed!") 
            || messages.get(1).getText().equals("username got removed!")));
    }

    @Test
    void save_shouldMakeAnnouncement_whenMemberLeft() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", true);
        chat.inviteMember(memberId, "username", ownerId);
        chat.leave(memberId, "username");

        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<UserMessage> msgs = messageRepository.getByAmount(chat.getId(), 0 , 20);
        assertTrue(chats.size() == 1);
        assertTrue(msgs.size() == 2);
        assertTrue((msgs.get(0).getText().equals("username left!") 
            || msgs.get(1).getText().equals("username left!")));
    }

    @Test
    void save_shouldDeleteChatAndMessages_whenChatDissolved() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", false);
        UserMessage userMessage = new UserMessage(chat.getId(), "test", UUID.randomUUID());
        chatRepository.save(chat);
        messageRepository.save(userMessage);

        chat.dissolve(ownerId);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<UserMessage> messages = messageRepository.getByAmount(chat.getId(), 0, 20);
        assertTrue(chats.isEmpty());
        assertTrue(messages.isEmpty());
    }

    @Test
    void save_shouldDeleteChatAndNotMessages_whenChatDissolvedAndMessagesInAnotherChat() {
        UUID ownerId = UUID.randomUUID();
        UUID anotherChatId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", false);
        UserMessage userMessage = new UserMessage(anotherChatId, "test", UUID.randomUUID());
        chatRepository.save(chat);
        messageRepository.save(userMessage);

        chat.dissolve(ownerId);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<UserMessage> messages = messageRepository.getByAmount(anotherChatId, 0, 20);
        assertTrue(chats.isEmpty());
        assertFalse(messages.isEmpty());
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
