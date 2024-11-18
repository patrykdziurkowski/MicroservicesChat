package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.patrykdziurkowski.microserviceschat.application.CreateChatCommand;
import com.patrykdziurkowski.microserviceschat.application.DeleteChatCommand;

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

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @ParameterizedTest
    @CsvSource({
            "toooooooooooooooooooLongChatName1,P@ssword1!", // chat name longer than 30 characters
            "ab,P@ssword1!",                                // chat name shorter than 3 characters
            "emojiName☺️,P@ssword1!",                        // chat name containing non-ascii character
            "white\tspace,P@ssword1!",                      // chat name containing tab in the middle
            "f@ncyName,P@ssword1!",                         // chat name containing something other than alphanumeric
            ",P@ssword1!",                                  // empty chat name
            "\t,P@ssword1!",                                // whitespace chat name
            "ab\t,P@ssword1!",                              // chat name containing minimum characters but with a whitespace

            "chatName123,password123",                      // password not containing special character
            "chatName123,p@sswo1",                          // password shorter than 8 characters
            "chatName123,p@ssworddd#",                      // password not containing a number
    })
    void createChat_shouldReturnBadRequest_whenInputInvalid(String chatName, String password) throws Exception {
        UUID currentUserId = UUID.randomUUID();
        ChatModel chatModel = new ChatModel(chatName, false, password);
        String chatData = objectMapper.writeValueAsString(chatModel);

        when(createChatCommand.execute(currentUserId, chatName, true, Optional.of(password)))
            .thenReturn(false);


        mockMvc.perform(post("/chats")
                .param("currentUserId", currentUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatData))
                .andExpect(status().isBadRequest());
    }

    @Test
void createChat_shouldReturnForbidden_whenChatCreationFails() throws Exception {
    UUID currentUserId = UUID.randomUUID();
    ChatModel chatModel = new ChatModel("ValidChatName", true, "!password123");
    String chatData = objectMapper.writeValueAsString(chatModel);

    when(createChatCommand.execute(currentUserId, "ValidChatName", true, Optional.of("!password123")))
            .thenReturn(false);

    mockMvc.perform(post("/chats")
            .param("currentUserId", currentUserId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(chatData))
            .andExpect(status().isForbidden());
}


    @Test
    void createChat_shouldReturnCreated_whenChatCreationSucceeds() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        ChatModel chatModel = new ChatModel("ValidChatName", true, "!password123");
        String chatData = objectMapper.writeValueAsString(chatModel);

        when(createChatCommand.execute(currentUserId, "ValidChatName", true, Optional.of("!password123")))
                .thenReturn(true);

        mockMvc.perform(post("/chats")
                .param("currentUserId", currentUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatData))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteChat_shouldReturnBadRequest_whenCurrentUserIdMissing() throws Exception {
        UUID chatId = UUID.randomUUID();

        mockMvc.perform(delete("/chats/{chatId}", chatId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteChat_shouldReturnNotFound_whenChatDeletionFails() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();

        when(deleteChatCommand.execute(currentUserId, chatId)).thenReturn(false);

        mockMvc.perform(delete("/chats/{chatId}", chatId)
                .param("currentUserId", currentUserId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteChat_shouldReturnNoContent_whenChatDeletionSucceeds() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();

        when(deleteChatCommand.execute(currentUserId, chatId)).thenReturn(true);

        mockMvc.perform(delete("/chats/{chatId}", chatId)
                .param("currentUserId", currentUserId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
