package com.patrykdziurkowski.microserviceschat.infrastructure.repositories;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;
import com.patrykdziurkowski.microserviceschat.presentation.ChatDbContainerBase;

@DataJpaTest
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ChatRepositoryImplTests extends ChatDbContainerBase {
    @Autowired
    private ChatRepositoryImpl chatRepository;
    @Autowired
    private MessageRepositoryImpl messageRepository;
    @Autowired
    private FavoriteChatRepositoryImpl favoriteChatRepository;
    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private ObjectMapper objectMapper;

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
        assertEquals(chatId, chatFromDb.getId());
    }

    @Test
    void getByMemberId_shouldReturnChat_whenExistsAndUserIsMember() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", true);
        chatRepository.save(chat);
        UUID memberId = UUID.randomUUID();
        chat.join(memberId, "username");

        chatRepository.save(chat);
        List<ChatRoom> chats = chatRepository.getByMemberId(memberId, 0, 20);

        assertTrue(chats.contains(chat));
    }

    @Test
    void getByMemberId_shouldReturnChat_whenExistsAndChatIsPublic() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", true);
        chatRepository.save(chat);
        UUID memberId = UUID.randomUUID();

        chatRepository.save(chat);
        List<ChatRoom> chats = chatRepository.getByMemberId(memberId, 0, 20);
        assertTrue(chats.contains(chat));
    }

    @Test
    void getByMemberId_shouldReturnNull_whenExistsAndChatIsPrivate() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        chatRepository.save(chat);
        UUID memberId = UUID.randomUUID();

        chatRepository.save(chat);
        List<ChatRoom> chats = chatRepository.getByMemberId(memberId, 0, 20);
        assertFalse(chats.contains(chat));
    }

    @Test
    void getByMemberId_shouldReturnChat_whenExistsAndUserIsMemberAndChatIsPrivate() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        chatRepository.save(chat);
        UUID memberId = UUID.randomUUID();
        chat.join(memberId, "username");

        chatRepository.save(chat);
        List<ChatRoom> chats = chatRepository.getByMemberId(memberId, 0, 20);
        assertTrue(chats.contains(chat));
    }

    @Test
    void getByMemberId_shouldReturnOnlySecondChat_whenExistsAndLastChatPositionIs1() {
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        ChatRoom secondChat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        chat.join(memberId, "username");
        secondChat.join(memberId, "username");
        chatRepository.save(chat);
        chatRepository.save(secondChat);

        List<ChatRoom> chats = chatRepository.getByMemberId(memberId, 1, 20);

        assertFalse(chats.contains(chat));
        assertTrue(chats.contains(secondChat));

    }

    @Test
    void getByMemberId_shouldOrderChatByFavoritesFirst_whenFavoritesExist() {
        UUID memberId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        ChatRoom secondChat = new ChatRoom(UUID.randomUUID(), "Chat", false);
        ChatRoom thirdChat = new ChatRoom(memberId, "Chat", false);
        chat.join(memberId, "username");
        secondChat.join(memberId, "username");
        chatRepository.save(chat);
        chatRepository.save(secondChat);
        chatRepository.save(thirdChat);
        List<ChatRoom> chatsBeforeFavorited = chatRepository.getByMemberId(memberId, 0, 20);
        favoriteChatRepository.save(FavoriteChatRoom.set(memberId, thirdChat).orElseThrow());

        List<ChatRoom> chatsAfterFavorited = chatRepository.getByMemberId(memberId, 0, 20);

        // Before
        assertTrue(chatsBeforeFavorited.contains(chat));
        assertTrue(chatsBeforeFavorited.contains(secondChat));
        assertTrue(chatsBeforeFavorited.contains(thirdChat));
        assertEquals(thirdChat, chatsBeforeFavorited.get(2));
        // After
        assertTrue(chatsAfterFavorited.contains(chat));
        assertTrue(chatsAfterFavorited.contains(secondChat));
        assertTrue(chatsAfterFavorited.contains(thirdChat));
        assertEquals(thirdChat, chatsAfterFavorited.get(0));
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
        assertEquals(2, chats.size());
    }

    @Test
    void save_shouldSaveOnly1Chat_whenSameChatAdded2Times() {
        ChatRoom chat = new ChatRoom(UUID.randomUUID(), "Chat", false);

        chatRepository.save(chat);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        assertFalse(chats.isEmpty());
        assertEquals(1, chats.size());
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
        List<UserMessage> messages = messageRepository.getByAmount(chat.getId(), 0, 20);
        assertEquals(1, chats.size());
        assertEquals(1, messages.size());
        assertEquals("username joined!", messages.get(0).getText());
    }

    @Test
    void save_shouldMakeAnnouncement_whenMemberInvitedAndJoined() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chat = new ChatRoom(ownerId, "Chat", true);
        chat.inviteMember(UUID.randomUUID(), "username", ownerId);

        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<UserMessage> messages = messageRepository.getByAmount(chat.getId(), 0, 20);
        assertEquals(1, chats.size());
        assertEquals(1, messages.size());
        assertEquals("username joined through invite!", messages.get(0).getText());
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
        List<UserMessage> messages = messageRepository.getByAmount(chat.getId(), 0, 20);
        assertEquals(1, chats.size());
        assertEquals(2, messages.size());
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
        List<UserMessage> msgs = messageRepository.getByAmount(chat.getId(), 0, 20);
        assertEquals(1, chats.size());
        assertEquals(2, msgs.size());
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
        FavoriteChatRoom favChat = FavoriteChatRoom.set(ownerId, chat).get();
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
        ChatRoom anotherChat = new ChatRoom(ownerId, "Chat", false);
        chatRepository.save(chat);
        chatRepository.save(anotherChat);
        FavoriteChatRoom favChat = FavoriteChatRoom.set(ownerId, anotherChat).get();
        favoriteChatRepository.save(favChat);

        chat.dissolve(ownerId);
        chatRepository.save(chat);

        List<ChatRoom> chats = chatRepository.get();
        List<FavoriteChatRoom> favChats = favoriteChatRepository.getByUserId(ownerId);
        assertEquals(1, chats.size());
        assertFalse(favChats.isEmpty());
    }

}
