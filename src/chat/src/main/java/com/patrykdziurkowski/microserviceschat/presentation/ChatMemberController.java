package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.InviteMemberCommand;
import com.patrykdziurkowski.microserviceschat.application.JoinChatCommand;
import com.patrykdziurkowski.microserviceschat.application.KickMemberCommand;
import com.patrykdziurkowski.microserviceschat.application.LeaveChatCommand;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

import jakarta.validation.Valid;

@RestController
public class ChatMemberController {
    private final InviteMemberCommand inviteMemberCommand;
    private final KickMemberCommand kickMemberCommand;
    private final JoinChatCommand joinChatCommand;
    private final LeaveChatCommand leaveChatCommand;

    public ChatMemberController(InviteMemberCommand inviteMemberCommand,
            KickMemberCommand kickMemberCommand,
            JoinChatCommand joinChatCommand,
            LeaveChatCommand leaveChatCommand) {
        this.inviteMemberCommand = inviteMemberCommand;
        this.kickMemberCommand = kickMemberCommand;
        this.joinChatCommand = joinChatCommand;
        this.leaveChatCommand = leaveChatCommand;
    }

    @PostMapping("/chats/{chatId}/members")
    public ResponseEntity<String> invite(Authentication authentication,
            @PathVariable UUID chatId,
            @RequestBody @Valid InvitedUserModel invitedUserData) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        boolean isMemberInvited = inviteMemberCommand.execute(currentUserId,
                chatId, invitedUserData.getUserId());
        if (isMemberInvited == false) {
            return new ResponseEntity<>("User invitation failed.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("User invitation was successful.", HttpStatus.CREATED);
    }

    @DeleteMapping("/chats/{chatId}/members/{memberId}")
    public ResponseEntity<MessageResponse> kick(Authentication authentication,
            @PathVariable UUID chatId,
            @PathVariable UUID memberId) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        boolean isMemberKicked = kickMemberCommand.execute(currentUserId, chatId, memberId);
        if (isMemberKicked == false) {
            return new ResponseEntity<>(new MessageResponse("Kicking member failed."), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(new MessageResponse("Member was kicked successfully."), HttpStatus.NO_CONTENT);
    }

    @PostMapping("/chats/{chatId}/user")
    public ResponseEntity<ChatRoomDto> join(Authentication authentication,
            @PathVariable UUID chatId,
            @RequestBody JoinChatModel joinChatModel) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        Optional<ChatRoom> joinedChatResult = joinChatCommand.execute(currentUserId,
                chatId,
                Optional.ofNullable(joinChatModel.getPassword()));
        if (joinedChatResult.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(
                ChatRoomDto.from(joinedChatResult.orElseThrow(), currentUserId),
                HttpStatus.CREATED);
    }

    @DeleteMapping("/chats/{chatId}/user")
    public ResponseEntity<ChatRoomDto> leave(Authentication authentication,
            @PathVariable UUID chatId) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        Optional<ChatRoom> chatLeftResult = leaveChatCommand.execute(currentUserId, chatId);
        if (chatLeftResult.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(
                ChatRoomDto.from(chatLeftResult.orElseThrow(), currentUserId),
                HttpStatus.OK);
    }

}
