package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrykdziurkowski.microserviceschat.application.ChatMessagesQuery;
import com.patrykdziurkowski.microserviceschat.application.ChatQuery;
import com.patrykdziurkowski.microserviceschat.application.MembersQuery;
import com.patrykdziurkowski.microserviceschat.application.PostMessageCommand;
import com.patrykdziurkowski.microserviceschat.application.RemoveMessageCommand;
import com.patrykdziurkowski.microserviceschat.application.User;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

@WebMvcTest(ChatMessageController.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@ContextConfiguration(classes = { ChatMessageController.class })
class ChatMessageControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostMessageCommand postMessageCommand;
    @MockBean
    private RemoveMessageCommand removeMessageCommand;
    @MockBean
    private ChatMessagesQuery chatMessagesQuery;
    @MockBean
    private ChatQuery chatQuery;
    @MockBean
    private MembersQuery membersQuery;

    private UUID currentUserId = UUID.randomUUID();
    private UUID chatId = UUID.randomUUID();
    private User chatMember = new User(currentUserId, "chatMember");
    private ChatRoom chat = new ChatRoom(chatId,"chat", false);

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @Test
    void addMessage_shouldReturnCreated_whenMessageIsAdded() throws Exception {
        NewMessageModel newMessage = new NewMessageModel("Hello, World!");

        when(postMessageCommand.execute(chatId, "Hello, World!", currentUserId))
                .thenReturn(Optional.of(new UserMessage(chatId, "Hello, World!", currentUserId)));
        when(chatQuery.execute(chatId)).thenReturn(Optional.of(chat));
        when(membersQuery.execute(chat.getMemberIds())).thenReturn(Optional.of(List.of(chatMember)));

        mockMvc.perform(post("/chats/{chatId}/messages", chatId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("currentUserUserName", "testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMessage)))
                .andExpect(status().isCreated());
    }

    @Test
    void addMessage_shouldReturnBadRequest_whenMessageCannotBeAdded() throws Exception {
        NewMessageModel newMessage = new NewMessageModel("Hello, World!");

        when(postMessageCommand.execute(chatId, "Hello, World!", currentUserId))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/chats/{chatId}/messages", chatId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("currentUserUserName", "testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMessage)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMessage_shouldReturnNoContent_whenMessageIsDeleted() throws Exception {
        UUID messageId = UUID.randomUUID();

        when(removeMessageCommand.execute(currentUserId, messageId))
                .thenReturn(true);

        mockMvc.perform(delete("/chats/{chatId}/messages/{messageId}", UUID.randomUUID(), messageId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMessage_shouldReturnForbidden_whenMessageCannotBeDeleted() throws Exception {
        UUID messageId = UUID.randomUUID();

        when(removeMessageCommand.execute(currentUserId, messageId))
                .thenReturn(false);

        mockMvc.perform(delete("/chats/{chatId}/messages/{messageId}", UUID.randomUUID(), messageId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMessages_shouldReturnOk_whenMessagesExist() throws Exception {
        UserMessage userMessage = new UserMessage(chatId, "testUser", UUID.randomUUID());
        List<UserMessage> messages = List.of(userMessage);

        when(chatMessagesQuery.execute(currentUserId, chatId, 0, 20))
                .thenReturn(Optional.of(messages));
        when(chatQuery.execute(chatId)).thenReturn(Optional.of(chat));
        when(membersQuery.execute(chat.getMemberIds())).thenReturn(Optional.of(List.of(chatMember)));

        mockMvc.perform(get("/chats/{chatId}/messages", chatId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("offset", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getMessages_shouldReturnOnlySecondMessage_whenOffsetIsOne() throws Exception {
        UserMessage userMessage = new UserMessage(chatId, "testUser", UUID.randomUUID());
        List<UserMessage> messages = List.of(userMessage);
        List<MessageDto> messagesDto = MessageDto.fromList(messages, currentUserId, List.of(chatMember));
        
        when(chatMessagesQuery.execute(currentUserId, chatId, 1, 20))
                .thenReturn(Optional.of(messages));
        when(chatQuery.execute(chatId)).thenReturn(Optional.of(chat));
        when(membersQuery.execute(chat.getMemberIds())).thenReturn(Optional.of(List.of(chatMember)));

        mockMvc.perform(get("/chats/{chatId}/messages", chatId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("offset", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(messagesDto)));
    }

    @Test
    void getMessages_shouldReturnNoContent_whenNoMessagesExist() throws Exception {
        UUID chatId = UUID.randomUUID();
        when(chatMessagesQuery.execute(currentUserId, chatId, 0, 20))
                .thenReturn(Optional.of(Collections.emptyList()));
        when(chatQuery.execute(chatId)).thenReturn(Optional.of(chat));
        when(membersQuery.execute(chat.getMemberIds())).thenReturn(Optional.of(List.of(chatMember)));
        

        

        mockMvc.perform(get("/chats/{chatId}/messages", chatId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("offset", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getMessages_shouldReturnBadRequest_whenNoOffsetIsNegative() throws Exception {
        UUID chatId = UUID.randomUUID();
        when(chatMessagesQuery.execute(currentUserId, chatId, 0, 20))
                .thenReturn(Optional.of(Collections.emptyList()));

        mockMvc.perform(get("/chats/{chatId}/messages", chatId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("offset", "-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMessages_shouldReturnForbidden_whenUserNotInChat() throws Exception {
        UUID chatId = UUID.randomUUID();

        when(chatMessagesQuery.execute(currentUserId, chatId, 0, 20))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/chats/{chatId}/messages", chatId)
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("offset", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
