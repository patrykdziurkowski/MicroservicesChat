package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrykdziurkowski.microserviceschat.application.InviteMemberCommand;
import com.patrykdziurkowski.microserviceschat.application.JoinChatCommand;
import com.patrykdziurkowski.microserviceschat.application.KickMemberCommand;
import com.patrykdziurkowski.microserviceschat.application.LeaveChatCommand;

@WebMvcTest(ChatMemberController.class)
@TestPropertySource(properties = {
        "jwt.secret=8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz"
})
@ContextConfiguration(classes = { ChatMemberController.class })
@Import(TestSecurityConfig.class)
class ChatMemberControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InviteMemberCommand inviteMemberCommand;

    @MockBean
    private KickMemberCommand kickMemberCommand;

    @MockBean
    private JoinChatCommand joinChatCommand;

    @MockBean
    private LeaveChatCommand leaveChatCommand;

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @Test
    void inviteMember_shouldReturnForbidden_whenInvitationFails() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        String invitedUserUserName = "testUser";
        InvitedUserModel invitedUserModel = new InvitedUserModel(UUID.randomUUID());
        String invitedUserData = objectMapper.writeValueAsString(invitedUserModel);

        when(inviteMemberCommand.execute(currentUserId, chatId, invitedUserModel.getUserId(), invitedUserUserName))
                .thenReturn(false);

        mockMvc.perform(post("/chats/{chatId}/members", chatId)
                .param("currentUserId", currentUserId.toString())
                .param("invitedUserUserName", invitedUserUserName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invitedUserData))
                .andExpect(status().isForbidden());
    }

    @Test
    void inviteMember_shouldReturnCreated_whenInvitationSucceeds() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        String invitedUserUserName = "testUser";
        InvitedUserModel invitedUserModel = new InvitedUserModel(UUID.randomUUID());

        when(inviteMemberCommand.execute(currentUserId, chatId, invitedUserModel.getUserId(), invitedUserUserName))
                .thenReturn(true);

        mockMvc.perform(post("/chats/{chatId}/members", chatId)
                .param("currentUserId", currentUserId.toString())
                .param("invitedUserUserName", invitedUserUserName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invitedUserModel)))
                .andExpect(status().isCreated());
    }

    @Test
    void kickMember_shouldReturnForbidden_whenKickFails() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        String memberUserName = "testMember";

        when(kickMemberCommand.execute(currentUserId, chatId, memberId, memberUserName))
                .thenReturn(false);

        mockMvc.perform(delete("/chats/{chatId}/members/{memberId}", chatId, memberId)
                .param("currentUserId", currentUserId.toString())
                .param("memberUserName", memberUserName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void kickMember_shouldReturnNoContent_whenKickSucceeds() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        String memberUserName = "testMember";

        when(kickMemberCommand.execute(currentUserId, chatId, memberId, memberUserName))
                .thenReturn(true);

        mockMvc.perform(delete("/chats/{chatId}/members/{memberId}", chatId, memberId)
                .param("currentUserId", currentUserId.toString())
                .param("memberUserName", memberUserName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void joinChat_shouldReturnBadRequest_whenJoinFails() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        String currentUserUserName = "currentUser";
        JoinChatModel joinChatModel = new JoinChatModel("wrongPassword");

        when(joinChatCommand.execute(currentUserId, chatId, currentUserUserName, Optional.of("wrongPassword")))
                .thenReturn(false);

        mockMvc.perform(post("/chats/{chatId}/user", chatId)
                .param("currentUserId", currentUserId.toString())
                .param("currentUserUserName", currentUserUserName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinChatModel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void joinChat_shouldReturnCreated_whenJoinSucceeds() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        String currentUserUserName = "currentUser";
        JoinChatModel joinChatModel = new JoinChatModel("correctP@ssword1");

        when(joinChatCommand.execute(currentUserId, chatId, currentUserUserName, Optional.of("correctP@ssword1")))
                .thenReturn(true);

        mockMvc.perform(post("/chats/{chatId}/user", chatId)
                .param("currentUserId", currentUserId.toString())
                .param("currentUserUserName", currentUserUserName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinChatModel)))
                .andExpect(status().isCreated());
    }

    @Test
    void leaveChat_shouldReturnForbidden_whenLeaveFails() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        String currentUserUserName = "currentUser";

        when(leaveChatCommand.execute(currentUserId, chatId, currentUserUserName))
                .thenReturn(false);

        mockMvc.perform(delete("/chats/{chatId}/user", chatId)
                .param("currentUserId", currentUserId.toString())
                .param("currentUserUserName", currentUserUserName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void leaveChat_shouldReturnNoContent_whenLeaveSucceeds() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        String currentUserUserName = "currentUser";

        when(leaveChatCommand.execute(currentUserId, chatId, currentUserUserName))
                .thenReturn(true);

        mockMvc.perform(delete("/chats/{chatId}/user", chatId)
                .param("currentUserId", currentUserId.toString())
                .param("currentUserUserName", currentUserUserName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
