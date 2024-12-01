package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class InviteMemberCommand {
    private final ChatRepository chatRepository;
    private final AuthenticationApiClient apiClient;

    public InviteMemberCommand(ChatRepository chatRepository,
            AuthenticationApiClient apiClient) {
        this.chatRepository = chatRepository;
        this.apiClient = apiClient;
    }

    public boolean execute(UUID currentUserId, UUID chatId, UUID invitedMemberId) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if (retrievedChat.isEmpty()) {
            return false;
        }
        ChatRoom chat = retrievedChat.get();
        Optional<String> invitedUserName = apiClient.sendUserNameRequest(invitedMemberId);
        if (invitedUserName.isEmpty()) {
            return false;
        }
        if (chat.inviteMember(invitedMemberId, invitedUserName.orElseThrow(), currentUserId) == false) {
            return false;
        }
        chatRepository.save(chat);
        return true;
    }
}
