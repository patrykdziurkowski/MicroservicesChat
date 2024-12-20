package com.patrykdziurkowski.microserviceschat.presentation.controllers;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.commands.PostMessageCommand;
import com.patrykdziurkowski.microserviceschat.application.commands.RemoveMessageCommand;
import com.patrykdziurkowski.microserviceschat.application.models.User;
import com.patrykdziurkowski.microserviceschat.application.queries.ChatMessagesQuery;
import com.patrykdziurkowski.microserviceschat.application.queries.ChatQuery;
import com.patrykdziurkowski.microserviceschat.application.queries.MembersQuery;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.presentation.dtos.MessageDto;
import com.patrykdziurkowski.microserviceschat.presentation.dtos.MessageResponse;
import com.patrykdziurkowski.microserviceschat.presentation.models.NewMessageModel;

import jakarta.validation.Valid;

@RestController
public class ChatMessageController {

    private final PostMessageCommand postMessageCommand;
    private final RemoveMessageCommand removeMessageCommand;
    private final ChatMessagesQuery chatMessagesQuery;
    private final MembersQuery membersQuery;
    private final ChatQuery chatQuery;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int NUMBER_OF_MESSAGES_TO_RETRIEVE = 20;

    public ChatMessageController(PostMessageCommand addMessageCommand,
            RemoveMessageCommand deleteMessageCommand,
            ChatMessagesQuery chatMessagesQuery,
            MembersQuery membersQuery,
            ChatQuery chatQuery,
            SimpMessagingTemplate messagingTemplate) {
        this.postMessageCommand = addMessageCommand;
        this.removeMessageCommand = deleteMessageCommand;
        this.chatMessagesQuery = chatMessagesQuery;
        this.membersQuery = membersQuery;
        this.chatQuery = chatQuery;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/chats/{chatId}/messages")
    public ResponseEntity<MessageDto> addMessage(Authentication authentication,
            @PathVariable UUID chatId,
            @RequestBody @Valid NewMessageModel newMessage) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        Optional<UserMessage> addedMessage = postMessageCommand.execute(chatId, newMessage.getContent(), currentUserId);
        if (addedMessage.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<ChatRoom> chat = chatQuery.execute(chatId);
        if (chat.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<List<User>> chatMembers = membersQuery.execute(chat.orElseThrow().getMemberIds());
        if (chatMembers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        MessageDto messagesDto = MessageDto.from(addedMessage.orElseThrow(), currentUserId,
                chatMembers.orElseThrow());
        messagingTemplate.convertAndSend("/chat/" + chatId, messagesDto);
        return new ResponseEntity<>(messagesDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/chats/{chatId}/messages/{messageId}")
    public ResponseEntity<MessageResponse> deleteMessage(
            Authentication authentication,
            @PathVariable UUID chatId,
            @PathVariable UUID messageId) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        boolean isMessageDeleted = removeMessageCommand.execute(currentUserId, messageId);
        if (!isMessageDeleted) {
            return new ResponseEntity<>(new MessageResponse("Message could not be deleted."), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(new MessageResponse("Message was deleted successfully."), HttpStatus.NO_CONTENT);
    }

    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(
            Authentication authentication,
            @PathVariable UUID chatId,
            @RequestParam(defaultValue = "0") int offset) {
        if (offset < 0) {
            return ResponseEntity.badRequest().build();
        }

        UUID currentUserId = UUID.fromString(authentication.getName());
        Optional<List<UserMessage>> messages = chatMessagesQuery.execute(
                currentUserId,
                chatId,
                offset,
                NUMBER_OF_MESSAGES_TO_RETRIEVE);
        if (messages.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Optional<ChatRoom> chat = chatQuery.execute(chatId);
        if (chat.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<List<User>> messageOwners = membersQuery
                .execute(messages.orElseThrow().stream()
                        .map(UserMessage::getOwnerId)
                        .filter(Objects::nonNull)
                        .toList());
        if (messageOwners.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<MessageDto> messagesDto = MessageDto.fromList(messages.orElseThrow(), currentUserId, messageOwners.get());
        return ResponseEntity.ok(messagesDto);
    }
}
