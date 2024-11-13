package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class InviteMemberCommand {
    private final ChatRepository chatRepository;

    public InviteMemberCommand(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public boolean execute(UUID currentUserId, UUID chatId, UUID invitedMemberId, String invitedMemberUsername) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if(retrievedChat.isEmpty()) {
            return false;
        }
        ChatRoom chat = retrievedChat.get();
        if(chat.inviteMember(invitedMemberId, invitedMemberUsername, currentUserId) == false) {
            return false;
        }
        chatRepository.save(chat);
        return true;
    }
}
