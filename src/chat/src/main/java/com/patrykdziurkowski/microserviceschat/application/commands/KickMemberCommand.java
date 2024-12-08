package com.patrykdziurkowski.microserviceschat.application.commands;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.ChatRepository;
import com.patrykdziurkowski.microserviceschat.application.interfaces.UserApiClient;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class KickMemberCommand {
    private final ChatRepository chatRepository;
    private final UserApiClient apiClient;

    public KickMemberCommand(ChatRepository chatRepository,
            UserApiClient apiClient) {
        this.chatRepository = chatRepository;
        this.apiClient = apiClient;
    }

    public boolean execute(UUID currentUserId, UUID chatId, UUID memberId) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if (retrievedChat.isEmpty()) {
            return false;
        }
        ChatRoom chat = retrievedChat.get();
        Optional<String> memberUserName = apiClient.sendUserNameRequest(memberId);
        if (memberUserName.isEmpty()) {
            return false;
        }
        if (chat.removeMember(memberId, memberUserName.orElseThrow(), currentUserId) == false) {
            return false;
        }
        chatRepository.save(chat);
        return true;
    }
}
