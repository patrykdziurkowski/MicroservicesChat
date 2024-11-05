package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.patrykdziurkowski.microserviceschat.application.ChangeUserNameCommand;
import com.patrykdziurkowski.microserviceschat.application.LoginQuery;
import com.patrykdziurkowski.microserviceschat.application.RegisterCommand;
import com.patrykdziurkowski.microserviceschat.domain.User;

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
        UserNameModel userModel = new UserNameModel(userName);
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(put("/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeUserName_shouldReturnBadRequest_whenNoAuthenticationToken() throws Exception {
        UserNameModel userModel = new UserNameModel("newUserName");
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
        String token = jwtTokenManager.generateToken(userId, userName);
        UserNameModel userModel = new UserNameModel("newUserName");
        String userData = objectMapper.writeValueAsString(userModel);
        when(changeUserNameCommand.execute(userId, userName)).thenReturn(false);

        mockMvc.perform(put("/username")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(userData))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeUserName_shouldReturnOk_whenSuccessfullyChanged() throws Exception {
        UUID userId = UUID.randomUUID();
        String userName = "oldUserName";
        String token = jwtTokenManager.generateToken(userId, userName);
        UserNameModel userModel = new UserNameModel("newUserName");
        String userData = objectMapper.writeValueAsString(userModel);
        when(changeUserNameCommand.execute(userId, userName)).thenReturn(true);

        mockMvc.perform(put("/username")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(userData))
                .andExpect(status().isOk());
    }

}
