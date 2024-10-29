package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrykdziurkowski.microserviceschat.application.LoginQuery;
import com.patrykdziurkowski.microserviceschat.application.RegisterCommand;

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
    @MockBean
    private RegisterCommand registerCommand;
    @MockBean
    private LoginQuery loginQuery;

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
                .content(userData)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturnCreated_whenInputValidButUserDidntGetRegistered() throws Exception {
        when(registerCommand.execute("validUserName", "P@ssword1!")).thenReturn(false);
        UserModel userModel = new UserModel("validUserName", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData)
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void register_shouldReturnCreated_whenInputValidAndUserGotRegistered() throws Exception {
        when(registerCommand.execute("validUserName", "P@ssword1!")).thenReturn(true);
        UserModel userModel = new UserModel("validUserName", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData)
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void login_shouldReturnBadRequest_whenInputInvalid() throws Exception {
        UserModel userModel = new UserModel("bad UserName", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnNotFound_whenLoginFails() throws Exception {
        when(loginQuery.execute("existingUser", "P@ssword1!")).thenReturn(false);
        UserModel userModel = new UserModel("existingUser", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_shouldReturnOkAndToken_whenLoginSucceeds() throws Exception {
        when(loginQuery.execute("existingUser", "P@ssword1!")).thenReturn(true);
        UserModel userModel = new UserModel("existingUser", "P@ssword1!");
        String userData = objectMapper.writeValueAsString(userModel);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }
}
