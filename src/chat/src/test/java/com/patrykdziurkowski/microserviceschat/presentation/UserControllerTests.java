package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrykdziurkowski.microserviceschat.application.ChangeUserNameCommand;

@WebMvcTest(ChatMessageController.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@ContextConfiguration(classes = { UserController.class })
class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChangeUserNameCommand changeUserNameCommand;

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @Test
    void changeUserName_shouldReturnForbidden_whenUserNameCouldntBeChanged() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        ChangeUserNameModel model = new ChangeUserNameModel("duplicateName");
        String data = objectMapper.writeValueAsString(model);
        when(changeUserNameCommand.execute(currentUserId, "duplicateName"))
                .thenReturn(false);

        mockMvc.perform(put("/username")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(data))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeUserName_shouldReturnOk_whenUserNameChanged() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        ChangeUserNameModel model = new ChangeUserNameModel("newUserName");
        String data = objectMapper.writeValueAsString(model);
        when(changeUserNameCommand.execute(currentUserId, "newUserName"))
                .thenReturn(true);

        mockMvc.perform(put("/username")
                .with(csrf())
                .with(user(currentUserId.toString()).password("").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(data))
                .andExpect(status().isOk());
    }

}
