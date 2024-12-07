package com.patrykdziurkowski.microserviceschat.presentation.controllers;

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

import com.patrykdziurkowski.microserviceschat.application.commands.CreateChatCommand;
import com.patrykdziurkowski.microserviceschat.application.commands.DeleteChatCommand;
import com.patrykdziurkowski.microserviceschat.application.models.User;
import com.patrykdziurkowski.microserviceschat.application.queries.ChatQuery;
import com.patrykdziurkowski.microserviceschat.application.queries.ChatsQuery;
import com.patrykdziurkowski.microserviceschat.application.queries.FavoritesQuery;
import com.patrykdziurkowski.microserviceschat.application.queries.MembersQuery;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;
import com.patrykdziurkowski.microserviceschat.presentation.dtos.ChatRoomDetailsDto;
import com.patrykdziurkowski.microserviceschat.presentation.dtos.ChatRoomDto;
import com.patrykdziurkowski.microserviceschat.presentation.models.ChatModel;

import jakarta.validation.Valid;

@RestController
public class ChatController {
    private final CreateChatCommand createChatCommand;
    private final DeleteChatCommand deleteChatCommand;
    private final ChatsQuery chatsQuery;
    private final ChatQuery chatQuery;
    private final MembersQuery membersQuery;
    private final FavoritesQuery favoritesQuery;
    private static final int NUMBER_OF_CHATS_TO_RETRIEVE = 20;

    public ChatController(CreateChatCommand createChatCommand, DeleteChatCommand deleteChatCommand,
            ChatsQuery chatsQuery, ChatQuery chatQuery, MembersQuery membersQuery, FavoritesQuery favoritesQuery) {
        this.createChatCommand = createChatCommand;
        this.deleteChatCommand = deleteChatCommand;
        this.chatsQuery = chatsQuery;
        this.chatQuery = chatQuery;
        this.membersQuery = membersQuery;
        this.favoritesQuery = favoritesQuery;
    }

    @PostMapping("/chats")
    public ResponseEntity<ChatRoomDto> createChat(Authentication authentication,
            @RequestBody @Valid ChatModel chatData) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        ChatRoom createdChat = createChatCommand.execute(currentUserId,
                chatData.getChatName(),
                chatData.getIsPublic(),
                Optional.ofNullable(chatData.getChatPassword()));

        return new ResponseEntity<>(
                ChatRoomDto.from(createdChat, currentUserId),
                HttpStatus.CREATED);
    }

    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<String> deleteChat(Authentication authentication,
            @PathVariable UUID chatId) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        boolean isChatDeleted = deleteChatCommand.execute(currentUserId, chatId);
        if (isChatDeleted == false) {
            return new ResponseEntity<>("Chat deletion failed.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/chats/load")
    public ResponseEntity<List<ChatRoomDto>> getChats(Authentication authentication,
            @RequestParam(defaultValue = "0") int offset) {
        if (offset < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UUID currentUserId = UUID.fromString(authentication.getName());
        List<ChatRoom> chats = chatsQuery.execute(currentUserId, offset, NUMBER_OF_CHATS_TO_RETRIEVE);
        if (chats.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<FavoriteChatRoom> favorites = favoritesQuery.execute(currentUserId);

        List<ChatRoomDto> chatsDto = ChatRoomDto.fromList(chats, favorites, currentUserId);
        return new ResponseEntity<>(chatsDto, HttpStatus.OK);
    }

    @GetMapping("/chats/{chatId}/details")
    public ResponseEntity<ChatRoomDetailsDto> getChatDetails(
            Authentication authentication,
            @PathVariable UUID chatId) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        Optional<ChatRoom> chat = chatQuery.execute(chatId);
        if (chat.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<List<User>> chatMembers = membersQuery.execute(chat.orElseThrow().getMemberIds());
        if (chatMembers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ChatRoomDetailsDto chatDto = ChatRoomDetailsDto.from(
                chat.orElseThrow(),
                chatMembers.orElseThrow(),
                currentUserId);
        return new ResponseEntity<>(chatDto, HttpStatus.OK);
    }

}
