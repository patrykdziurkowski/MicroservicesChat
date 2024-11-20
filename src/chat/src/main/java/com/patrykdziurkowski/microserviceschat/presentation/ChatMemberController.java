package com.patrykdziurkowski.microserviceschat.presentation;


import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.InviteMemberCommand;
import com.patrykdziurkowski.microserviceschat.application.JoinChatCommand;
import com.patrykdziurkowski.microserviceschat.application.KickMemberCommand;
import com.patrykdziurkowski.microserviceschat.application.LeaveChatCommand;

import jakarta.validation.Valid;

@RestController
public class ChatMemberController {
    private InviteMemberCommand inviteMemberCommand;
    private KickMemberCommand kickMemberCommand;
    private JoinChatCommand joinChatCommand;
    private LeaveChatCommand leaveChatCommand;


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
    public ResponseEntity<String> invite(@RequestParam UUID currentUserId,
            @RequestParam String invitedUserUserName,
            @PathVariable UUID chatId,
            @RequestBody @Valid InvitedUserModel invitedUserData) {
        boolean isMemberInvited = inviteMemberCommand.execute(currentUserId,
            chatId, invitedUserData.getUserId(), invitedUserUserName);
        if(isMemberInvited == false) {
            return new ResponseEntity<>("User invitation failed.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("User invitation was successful.", HttpStatus.CREATED);
    }

    @DeleteMapping("/chats/{chatId}/members/{memberId}")
    public ResponseEntity<String> kick(@RequestParam UUID currentUserId,
            @RequestParam String memberUserName,
            @PathVariable UUID chatId,
            @PathVariable UUID memberId) {
        boolean isMemberKicked = kickMemberCommand.execute(currentUserId, chatId, memberId, memberUserName);
        if(isMemberKicked == false) {
            return new ResponseEntity<>("Kicking member failed.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Member was kicked successfully.", HttpStatus.NO_CONTENT);
    }

    @PostMapping("/chats/{chatId}/user")
    public ResponseEntity<String> join(@RequestParam UUID currentUserId,
            @RequestParam String currentUserUserName,
            @PathVariable UUID chatId,
            @RequestBody JoinChatModel joinChatModel) {
        
        boolean didUserJoin = joinChatCommand.execute(currentUserId, 
            chatId, 
            currentUserUserName, 
            Optional.ofNullable(joinChatModel.getPassword()));
        if(didUserJoin == false) {
            return new ResponseEntity<>("User did not join chat.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("User joined successfully.", HttpStatus.CREATED);
    }

    @DeleteMapping("/chats/{chatId}/user")
    public ResponseEntity<String> leave(@RequestParam UUID currentUserId,
            @RequestParam String currentUserUserName,
            @PathVariable UUID chatId) {
        boolean didMemberLeave = leaveChatCommand.execute(currentUserId, chatId, currentUserUserName);
        if(didMemberLeave == false) {
            return new ResponseEntity<>("Member did not leave chat.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Member left chat successfully.", HttpStatus.NO_CONTENT);
    }

}
