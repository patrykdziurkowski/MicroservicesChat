package com.patrykdziurkowski.microserviceschat.presentation.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
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
import com.patrykdziurkowski.microserviceschat.application.commands.ChangeUserNameCommand;
import com.patrykdziurkowski.microserviceschat.application.models.User;
import com.patrykdziurkowski.microserviceschat.application.queries.SearchUsersQuery;
import com.patrykdziurkowski.microserviceschat.presentation.dtos.UserDto;
import com.patrykdziurkowski.microserviceschat.presentation.models.ChangeUserNameModel;

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
    private SearchUsersQuery searchUsersQuery;
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

    @Test
    void getUsers_shouldReturnBadRequest_whenNumberIsZero() throws Exception {
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(0))
                .queryParam("offset", String.valueOf(0))
                .queryParam("filter", "filter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenNumberNegative() throws Exception {
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(-1))
                .queryParam("offset", String.valueOf(0))
                .queryParam("filter", "filter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenNumberTooHigh() throws Exception {
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(21))
                .queryParam("offset", String.valueOf(0))
                .queryParam("filter", "filter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenOffsetNegative() throws Exception {
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(20))
                .queryParam("offset", String.valueOf(-1))
                .queryParam("filter", "filter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenFilterTooLong() throws Exception {
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(20))
                .queryParam("offset", String.valueOf(0))
                .queryParam("filter", "this16characters"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenNumberNotProvided() throws Exception {
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("offset", String.valueOf(0))
                .queryParam("filter", "filter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenOffsetNotProvided() throws Exception {
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(20))
                .queryParam("filter", "filter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenFilterIsEmptyString() throws Exception {
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(20))
                .queryParam("offset", String.valueOf(0))
                .queryParam("filter", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_shouldReturnOk_whenFilterNotProvided() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(new User(UUID.randomUUID(), "userName1"));
        users.add(new User(UUID.randomUUID(), "userName2"));
        users.add(new User(UUID.randomUUID(), "userName3"));
        when(searchUsersQuery.execute(20, 0, Optional.empty())).thenReturn(Optional.of(users));

        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(20))
                .queryParam("offset", String.valueOf(0)))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers_shouldReturnOk_whenEverythingIsValid() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(new User(UUID.randomUUID(), "userName1"));
        users.add(new User(UUID.randomUUID(), "userName2"));
        users.add(new User(UUID.randomUUID(), "userName3"));
        when(searchUsersQuery.execute(20, 0, Optional.of("filter"))).thenReturn(Optional.of(users));

        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(20))
                .queryParam("offset", String.valueOf(0))
                .queryParam("filter", "filter"))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers_shouldReturnData_whenEverythingIsValid() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(new User(UUID.randomUUID(), "userName1"));
        users.add(new User(UUID.randomUUID(), "userName2"));
        users.add(new User(UUID.randomUUID(), "userName3"));
        when(searchUsersQuery.execute(20, 0, Optional.empty())).thenReturn(Optional.of(users));
        List<UserDto> userDtos = UserDto.fromList(users);

        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(20))
                .queryParam("offset", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userDtos)));
    }

}
