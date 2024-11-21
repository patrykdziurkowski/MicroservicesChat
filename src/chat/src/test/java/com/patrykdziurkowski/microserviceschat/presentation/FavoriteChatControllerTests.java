package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.patrykdziurkowski.microserviceschat.application.SetFavoriteCommand;
import com.patrykdziurkowski.microserviceschat.application.UnsetFavoriteCommand;

@WebMvcTest(FavoriteChatController.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@ContextConfiguration(classes = { FavoriteChatController.class })
class FavoriteChatControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SetFavoriteCommand setFavoriteCommand;
    @MockBean
    private UnsetFavoriteCommand unsetFavoriteCommand;

    private UUID currentUserId = UUID.randomUUID();

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @Test
    void addFavorite_shouldReturnCreated_whenSuccessful() throws Exception {
        UUID chatId = UUID.randomUUID();

        when(setFavoriteCommand.execute(currentUserId, chatId)).thenReturn(true);

        mockMvc.perform(post("/favorites")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("chatId", chatId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void addFavorite_shouldReturnBadRequest_whenFailed() throws Exception {
        UUID chatId = UUID.randomUUID();

        when(setFavoriteCommand.execute(currentUserId, chatId)).thenReturn(false);

        mockMvc.perform(post("/favorites")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("chatId", chatId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeFavorite_shouldReturnNoContent_whenSuccessful() throws Exception {
        UUID chatId = UUID.randomUUID();

        when(unsetFavoriteCommand.execute(currentUserId, chatId)).thenReturn(true);

        mockMvc.perform(delete("/favorites")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("chatId", chatId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFavorite_shouldReturnBadRequest_whenFailed() throws Exception {
        UUID chatId = UUID.randomUUID();

        when(unsetFavoriteCommand.execute(currentUserId, chatId)).thenReturn(false);

        mockMvc.perform(delete("/favorites")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .param("chatId", chatId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
