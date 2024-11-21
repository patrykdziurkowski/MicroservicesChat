package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.ChatsQuery;
import com.patrykdziurkowski.microserviceschat.application.CreateChatCommand;
import com.patrykdziurkowski.microserviceschat.application.DeleteChatCommand;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

import jakarta.validation.Valid;

@RestController
public class ChatController {
    private final CreateChatCommand createChatCommand;
    private final DeleteChatCommand deleteChatCommand;
    private final ChatsQuery chatsQuery;
    private static final int NUMBER_OF_CHATS_TO_RETRIEVE = 20;

    public ChatController(CreateChatCommand createChatCommand,
            DeleteChatCommand deleteChatCommand,
            ChatsQuery chatsQuery) {
        this.createChatCommand = createChatCommand;
        this.deleteChatCommand = deleteChatCommand;
        this.chatsQuery = chatsQuery;
    }

    @PostMapping("/chats")
    public ResponseEntity<String> createChat(Authentication authentication,
                                             @RequestBody @Valid ChatModel chatData) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        boolean isChatCreated = createChatCommand.execute(currentUserId,
            chatData.getChatName(),
            chatData.getIsPublic(),
            Optional.ofNullable(chatData.getChatPassword()));
        if(isChatCreated == false) {
            return new ResponseEntity<>("Chat creation failed.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Chat creation was successful.", HttpStatus.CREATED);
    }

    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<String> deleteChat(Authentication authentication,
                                             @PathVariable UUID chatId) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        boolean isChatDeleted = deleteChatCommand.execute(currentUserId, chatId);
        if(isChatDeleted == false) {
            return new ResponseEntity<>("Chat deletion failed.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/chats/load")
    public ResponseEntity<List<ChatRoom>> getChats(Authentication authentication,
                                                   @RequestParam(defaultValue = "0") int offset) {
        if(offset < 0) {
            return ResponseEntity.badRequest().build();
        }
        UUID currentUserId = UUID.fromString(authentication.getName());
        List<ChatRoom> chats = chatsQuery.execute(currentUserId, offset, NUMBER_OF_CHATS_TO_RETRIEVE);
        if(chats.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(chats);
    }

}
