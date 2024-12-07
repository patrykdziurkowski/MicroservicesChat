package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrykdziurkowski.microserviceschat.application.commands.ChangeUserNameCommand;
import com.patrykdziurkowski.microserviceschat.application.commands.RegisterCommand;
import com.patrykdziurkowski.microserviceschat.application.queries.LoginQuery;
import com.patrykdziurkowski.microserviceschat.application.queries.MembersQuery;
import com.patrykdziurkowski.microserviceschat.application.queries.UserQuery;
import com.patrykdziurkowski.microserviceschat.application.queries.UsersQuery;
import com.patrykdziurkowski.microserviceschat.domain.User;
import com.patrykdziurkowski.microserviceschat.presentation.models.GetUserModel;
import com.patrykdziurkowski.microserviceschat.presentation.models.UserModel;
import com.patrykdziurkowski.microserviceschat.presentation.models.UserNameModel;

import io.jsonwebtoken.lang.Collections;

@WebMvcTest(UserController.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@Import(WebSecurityConfig.class)
@ContextConfiguration(classes = { UserController.class, JwtTokenManager.class })
class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtTokenManager jwtTokenManager;

    @MockBean
    private RegisterCommand registerCommand;
    @MockBean
    private LoginQuery loginQuery;
    @MockBean
    private UserQuery userQuery;
    @MockBean
    private UsersQuery usersQuery;
    @MockBean
    private MembersQuery membersQuery;
    @MockBean
    private ChangeUserNameCommand changeUserNameCommand;

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @ParameterizedTest
    @CsvSource({
            "tooLongUserName1,P@ssword1!", // username longer than 15 characters
            "ab,P@ssword1!", // username shorter than 3 characters
            "emojiName☺️,P@ssword1!", // username containing non-ascii character
            "white space,P@ssword1!", // username containing space in the middle
            "white\tspace,P@ssword1!", // username containing tab in the middle
            "f@ncyName,P@ssword1!", // username containing something other than alphanumeric
            ",P@ssword1!", // empty username
            "\t,P@ssword1!", // whitespace username
            "ab\t,P@ssword1!", // username containing minimum characters but with a whitespace

            "userName123,password123", // password not containing special character
            "userName123,p@sswo1", // password shorter than 8 characters
            "userName123,p@ssworddd#", // password not containing a number
    })
    void register_shouldReturnFalse_whenInputInvalid(String userName, String password) throws Exception {
        UserModel userModel = new UserModel(userName, password);
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturnCreated_whenInputValidButUserDidntGetRegistered() throws Exception {
        when(registerCommand.execute("validUserName", "P@ssword1!")).thenReturn(false);
        UserModel userModel = new UserModel("validUserName", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_shouldReturnCreated_whenInputValidAndUserGotRegistered() throws Exception {
        when(registerCommand.execute("validUserName", "P@ssword1!")).thenReturn(true);
        UserModel userModel = new UserModel("validUserName", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isCreated());
    }

    @Test
    void login_shouldReturnBadRequest_whenInputInvalid() throws Exception {
        UserModel userModel = new UserModel("bad UserName", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnNotFound_whenLoginFails() throws Exception {
        when(loginQuery.execute("existingUser", "P@ssword1!")).thenReturn(Optional.empty());
        UserModel userModel = new UserModel("existingUser", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_shouldReturnOkAndToken_whenLoginSucceeds() throws Exception {
        User user = new User("existingUser", "dummyPassword1!");
        when(loginQuery.execute("existingUser", "P@ssword1!"))
                .thenReturn(Optional.of(user));
        UserModel userModel = new UserModel("existingUser", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    @Test
    void validateToken_shouldReturnBadRequest_whenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/authenticate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateToken_shouldReturnOkAndUserId_whenValidTokenProvided() throws Exception {
        UUID userId = UUID.randomUUID();
        String userName = "oldUserName";
        String token = jwtTokenManager.generateToken(userId, userName);

        mockMvc.perform(get("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(userId.toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "tooLongUserName1", // username longer than 15 characters
            "ab", // username shorter than 3 characters
            "emojiName☺️", // username containing non-ascii character
            "white space", // username containing space in the middle
            "white\tspace", // username containing tab in the middle
            "f@ncyName", // username containing something other than alphanumeric
            "", // empty username
            "\t", // whitespace username
            "ab\t", // username containing minimum characters but with a whitespace
    })
    void changeUserName_shouldReturnBadRequest_whenUserNameInvalid(String userName) throws Exception {
        UserNameModel userModel = new UserNameModel(UUID.randomUUID(), userName);
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(put("/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeUserName_shouldReturnBadRequest_whenNoUserId() throws Exception {
        UserNameModel userModel = new UserNameModel(null, "newUserName");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(put("/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeUserName_shouldReturnForbidden_whenNotAllowedToChangeIt() throws Exception {
        UUID userId = UUID.randomUUID();
        String userName = "oldUserName";
        UserNameModel userModel = new UserNameModel(userId, "newUserName");
        String userData = objectMapper.writeValueAsString(userModel);
        when(changeUserNameCommand.execute(userId, userName)).thenReturn(false);

        mockMvc.perform(put("/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeUserName_shouldReturnOk_whenSuccessfullyChanged() throws Exception {
        UUID userId = UUID.randomUUID();
        UserNameModel userModel = new UserNameModel(userId, "newUserName");
        String userData = objectMapper.writeValueAsString(userModel);
        when(changeUserNameCommand.execute(userId, "newUserName")).thenReturn(true);

        mockMvc.perform(put("/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isOk());
    }

    @Test
    void getUser_shouldReturnUserDetails_whenUserExists() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User("testUser", "P@ssword1");
        GetUserModel expectedModel = new GetUserModel(user.getId(), "testUser");

        when(userQuery.execute(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedModel)));
    }

    @Test
    void getUser_shouldReturnNoContent_whenUserDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userQuery.execute(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
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
        users.add(new User("userName1", "passwordHash"));
        users.add(new User("userName2", "passwordHash"));
        users.add(new User("userName3", "passwordHash"));
        List<UserDto> userDtos = UserDto.fromList(users);
        when(usersQuery.execute(20, 0, Optional.empty()))
                .thenReturn(users);

        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER"))
                .queryParam("number", String.valueOf(20))
                .queryParam("offset", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userDtos)));
    }

    @Test
    void getMembers_shouldReturnEmptyList_whenNoIdsProvided() throws Exception {
        String emptyListJson = objectMapper.writeValueAsString(Collections.emptyList());
        when(membersQuery.execute(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyListJson)
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Collections.emptyList())));
    }

    @Test
    void getMembers_shouldReturnMappedMembers_whenIdsProvided() throws Exception {
        User user1 = new User("userName1", "passwordHash");
        User user2 = new User("userName2", "passwordHash");
        List<UUID> memberIds = List.of(user1.getId(), user2.getId());
        List<User> members = List.of(user1, user2);
        List<UserDto> memberDtos = UserDto.fromList(members);
        when(membersQuery.execute(memberIds)).thenReturn(members);

        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberIds))
                .with(csrf())
                .with(user(UUID.randomUUID().toString()).password("").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(memberDtos)));
    }

}
