package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.List;
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

import com.patrykdziurkowski.microserviceschat.application.ChatMessagesQuery;
import com.patrykdziurkowski.microserviceschat.application.PostMessageCommand;
import com.patrykdziurkowski.microserviceschat.application.RemoveMessageCommand;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

import jakarta.validation.Valid;

@RestController
public class ChatMessageController {

    private final PostMessageCommand postMessageCommand;
    private final RemoveMessageCommand removeMessageCommand;
    private final ChatMessagesQuery chatMessagesQuery;

    private static final int NUMBER_OF_MESSAGES_TO_RETRIEVE = 20;

    public ChatMessageController(PostMessageCommand addMessageCommand, 
                                RemoveMessageCommand deleteMessageCommand,
                                ChatMessagesQuery chatMessagesQuery) {
        this.postMessageCommand = addMessageCommand;
        this.removeMessageCommand = deleteMessageCommand;
        this.chatMessagesQuery = chatMessagesQuery;
    }

    @PostMapping("/chats/{chatId}/messages")
    public ResponseEntity<String> addMessage(Authentication authentication,
                                             @PathVariable UUID chatId,
                                             @RequestBody @Valid NewMessageModel newMessage) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        boolean isMessageAdded = postMessageCommand.execute(chatId, newMessage.getContent(), currentUserId);
        if(isMessageAdded == false) {
            return new ResponseEntity<>("Message could not be added.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Message added successfully.", HttpStatus.CREATED);
    }

    @DeleteMapping("/chats/{chatId}/messages/{messageId}")
    public ResponseEntity<String> deleteMessage(Authentication authentication,
                                                @PathVariable UUID messageId) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        boolean isMessageDeleted = removeMessageCommand.execute(currentUserId, messageId);
        if(isMessageDeleted == false) {
            return new ResponseEntity<>("Message could not be deleted.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Message deleted successfully.", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(Authentication authentication,
                                                        @PathVariable UUID chatId,
                                                        @RequestParam(defaultValue = "0") int offset) {
        if(offset < 0) {
            return ResponseEntity.badRequest().build();
        }
        List<UserMessage> messages = chatMessagesQuery.execute(chatId, offset, NUMBER_OF_MESSAGES_TO_RETRIEVE);
        if(messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        UUID currentUserId = UUID.fromString(authentication.getName());
        List<MessageDto> messagesDto = MessageDto.fromList(messages, currentUserId);
        return ResponseEntity.ok(messagesDto);
    }
}
