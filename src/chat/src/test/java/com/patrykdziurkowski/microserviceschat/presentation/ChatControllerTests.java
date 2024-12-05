package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrykdziurkowski.microserviceschat.application.ChatQuery;
import com.patrykdziurkowski.microserviceschat.application.ChatsQuery;
import com.patrykdziurkowski.microserviceschat.application.CreateChatCommand;
import com.patrykdziurkowski.microserviceschat.application.DeleteChatCommand;
import com.patrykdziurkowski.microserviceschat.application.FavoritesQuery;
import com.patrykdziurkowski.microserviceschat.application.MembersQuery;
import com.patrykdziurkowski.microserviceschat.application.User;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@WebMvcTest(ChatController.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@ContextConfiguration(classes = { ChatController.class })
class ChatControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateChatCommand createChatCommand;
    @MockBean
    private DeleteChatCommand deleteChatCommand;
    @MockBean
    private ChatsQuery chatsQuery;
    @MockBean
    private ChatQuery chatQuery;
    @MockBean
    private MembersQuery membersQuery;
    @MockBean
    private FavoritesQuery favoritesQuery;

    private UUID currentUserId = UUID.randomUUID();

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @ParameterizedTest
    @CsvSource({
            "toooooooooooooooooooLongChatName1,P@ssword1!", // chat name longer than 30 characters
            "ab,P@ssword1!", // chat name shorter than 3 characters
            "emojiName☺️,P@ssword1!", // chat name containing non-ascii character
            "white\tspace,P@ssword1!", // chat name containing tab in the middle
            "f@ncyName,P@ssword1!", // chat name containing something other than alphanumeric
            ",P@ssword1!", // empty chat name
            "\t,P@ssword1!", // whitespace chat name
            "ab\t,P@ssword1!", // chat name containing minimum characters but with a whitespace

            "chatName123,password123", // password not containing special character
            "chatName123,p@sswo1", // password shorter than 8 characters
            "chatName123,p@ssworddd#", // password not containing a number
    })
    void createChat_shouldReturnBadRequest_whenInputInvalid(String chatName, String password) throws Exception {
        ChatModel chatModel = new ChatModel(chatName, false, password);
        ChatRoom chatRoom = new ChatRoom(currentUserId, chatName, false);
        String chatData = objectMapper.writeValueAsString(chatModel);

        when(createChatCommand.execute(currentUserId, chatName, true, Optional.of(password)))
                .thenReturn(chatRoom);

        mockMvc.perform(post("/chats")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatData))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createChat_shouldReturnCreated_whenChatCreationSucceeds() throws Exception {
        ChatModel chatModel = new ChatModel("ValidChatName", true, "!password123");
        ChatRoom chatRoom = new ChatRoom(currentUserId, "ValidChatName", false);
        String chatData = objectMapper.writeValueAsString(chatModel);

        when(createChatCommand.execute(currentUserId, "ValidChatName", true, Optional.of("!password123")))
                .thenReturn(chatRoom);

        mockMvc.perform(post("/chats")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatData))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteChat_shouldReturnNotFound_whenChatDeletionFails() throws Exception {
        UUID chatId = UUID.randomUUID();

        when(deleteChatCommand.execute(currentUserId, chatId)).thenReturn(false);

        mockMvc.perform(delete("/chats/{chatId}", chatId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteChat_shouldReturnNoContent_whenChatDeletionSucceeds() throws Exception {
        UUID chatId = UUID.randomUUID();

        when(deleteChatCommand.execute(currentUserId, chatId)).thenReturn(true);

        mockMvc.perform(delete("/chats/{chatId}", chatId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getChats_shouldReturnOk_whenChatsExist() throws Exception {
        ChatRoom chatRoom = new ChatRoom(UUID.randomUUID(), "Test Chat", true);
        List<ChatRoom> chats = List.of(chatRoom);
        List<ChatRoomDto> chatsDto = ChatRoomDto.fromList(chats, currentUserId);

        when(chatsQuery.execute(currentUserId, 0, 20)).thenReturn(chats);

        mockMvc.perform(get("/chats/load")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("offset", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(chatsDto)));
    }

    @Test
    void getChats_shouldReturnNoContent_whenNoChatsExist() throws Exception {
        when(chatsQuery.execute(currentUserId, 0, 20)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/chats/load")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("offset", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getChats_shouldReturnBadRequest_whenOffsetIsInvalid() throws Exception {
        mockMvc.perform(get("/chats/load")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("offset", "-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getChatDetails_shouldReturnNotFound_whenChatNotFound() throws Exception {
        UUID chatId = UUID.randomUUID();
        when(chatQuery.execute(chatId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/chats/" + chatId + "/details")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getChatDetails_shouldReturnNotFound_whenChatMembersNotFound() throws Exception {
        ChatRoom chat = new ChatRoom(currentUserId, "name", false);
        when(chatQuery.execute(chat.getId())).thenReturn(Optional.of(chat));
        when(membersQuery.execute(chat.getMemberIds())).thenReturn(Optional.empty());

        mockMvc.perform(get("/chats/" + chat.getId() + "/details")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getChatDetails_shouldReturnOkAndDto_whenAllGoesWell() throws Exception {
        User user = new User(currentUserId, "someUserName1");
        ChatRoom chat = new ChatRoom(currentUserId, "name", false);
        ChatRoomDetailsDto dto = ChatRoomDetailsDto.from(chat, List.of(user), currentUserId);
        when(chatQuery.execute(chat.getId())).thenReturn(Optional.of(chat));
        when(membersQuery.execute(chat.getMemberIds())).thenReturn(Optional.of(List.of(user)));

        mockMvc.perform(get("/chats/" + chat.getId() + "/details")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

}
